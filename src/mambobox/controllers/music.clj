(ns mambobox.controllers.music
  (:use [mambobox.views.music-search :only [music-search-view]]
        [mambobox.views.music-detail :only [music-detail-view]]
        [mambobox.views.music-upload :only [music-upload-view]]
        [ring.util.response]
        [mambobox.utils :only [defnlog dlet]]
        [slingshot.slingshot :only [throw+ try+]]
        [clojure.string :only [lower-case]])
  (:require [mambobox.utils :as utils]
            [mambobox.data-access :as data]
            [clojure.tools.logging :as log]
            [fuzzy-string.core :as fuzz-str]
            [clojure.data.json :as json]
            [mambobox.config :as config]))


(defn accept-song-for-query? [song qstring]
  (let [song-name (lower-case (get song :name))
        song-artist (lower-case (get song :artist))]                              
    (or (> (fuzz-str/dice song-name qstring) 0.5) 
        (> (fuzz-str/dice song-artist qstring) 0.5)))) 
                          
(defn song-contains-tag? [song tag]
  (let [tags (get song :tags)]
    (some #{tag} tags)))

(defn get-cant-pages [col page-size]
  (let [col-size (count col)]
    (+ (quot col-size page-size) 
       (if (> (mod col-size page-size) 0) 1 0))))

(defn get-collection-page [col cur-page page-size]
  (let [col-size (count col)
        num-pages (get-cant-pages col page-size)
        first-song (inc (* (dec cur-page) page-size))
        last-song (dec (+ first-song page-size))]
    (utils/sub-list col first-song last-song)))
        

(defn search-music [username q tag cur-page page-size all-songs]
  (let [processed-q (when q (lower-case q))
        query-filtered-songs (if (not (empty? q)) 
                               (filter (fn [song]
                                         (accept-song-for-query? song processed-q))
                                       all-songs)
                               all-songs)
        tag-filtered-songs (if (not (empty? tag)) 
                             (filter (fn [song]
                                       (song-contains-tag? song tag))
                                     query-filtered-songs)
                             query-filtered-songs)
        num-pages (get-cant-pages tag-filtered-songs page-size)
        cur-page-songs (get-collection-page tag-filtered-songs cur-page page-size)]
    (log/info username "searchig for [" q "] with tag [" tag "] retrieved" (count tag-filtered-songs) "songs. Page:" cur-page)
    {:num-pages num-pages
     :songs-found cur-page-songs}))

(defn music-search [username q tag cur-page]
  (let [cur-page (if cur-page (utils/parse-int cur-page) 1)
        result (search-music username q tag cur-page config/result-page-size (data/get-all-songs))
        songs-found (:songs-found result)
        num-pages (:num-pages result)]
        (music-search-view username songs-found q tag cur-page num-pages)))

(defn music-id [username user-id song-id]
  (let [song (data/get-song-by-id song-id)]
    (data/track-song-access song-id)
    (data/add-song-to-visited user-id song-id)
    (log/info username "seeing" (:name song) "[" song-id "]")
    (music-detail-view username song)))

(defn edit-music [username id song-name artist] 
  (let [song (data/update-song id song-name artist)]
    (log/info username "editing song [" id "] with new name : [" song-name "] and new artist : [" artist "]")
    (music-detail-view username song)))

(defn upload-page [username]
  (music-upload-view username))

(defn upload-file [username file]
  (try+
   (let [file-map (first file)
         file-name (file-map :filename)
         temp-file (file-map :tempfile)
         size (file-map :size)
         generated-file-name (utils/gen-uuid temp-file) ;; md5sum
         metadata (utils/get-metadata temp-file)
         metadata-tags (when metadata
                         (metadata :tags))
         title-tag (when metadata-tags
                     (first (metadata-tags :title)))
         artist-tag (when metadata-tags
                      (first (metadata-tags :artist)))
         song-name (if (not (empty? title-tag)) title-tag file-name)
         song-artist (if (not (empty? artist-tag)) artist-tag "Desconocido")
         existing-song-for-sum (data/get-song-by-file-name generated-file-name)]
     (if existing-song-for-sum
       (do  
         (log/warn username "has uploaded a file:[" file-name "] of size [" size "] that is already on mambobox so skipping.")
         (throw+ {:type :upload-fail
                  :message (str "El archivo ya existe con nombre de tema : " (get existing-song-for-sum :name))
                  :filename file-name
                  :size size}))
       (do 
         (utils/save-file-to-disk file-map generated-file-name mambobox.config/music-dir)
         (let [created-song (data/save-song song-name
                                            song-artist
                                            file-name
                                            generated-file-name username)] 
           (log/info username "uploaded a file:[" file-name "] of size [" size "]")
           (log/info "FS generated name : " generated-file-name)
           (when (not metadata-tags) (log/info "We couldn't find any file ID3 tag"))
           (json/write-str {:files [{:name file-name
                                     :size size
                                     :url (str "/music/" (get created-song :_id))}]})))))
   (catch [:type :upload-fail] {:keys [message filename size]}       
     (json/write-str {:files [{:name filename
                               :size size
                               :error message}]}))))



(defn add-tag [username song-id tag-name]
  (data/add-song-tag song-id tag-name)
  (log/info username "has tagged" song-id "with" tag-name)
  {:status 200})

(defn delete-tag [username song-id tag-name]
  (data/del-song-tag song-id tag-name)
  (log/info username "has deleted from" song-id "tag" tag-name)
  {:status 200})

(defn is-youtube-mobile-link? [link]
   (re-matches #".*youtu.be/[a-zA-Z0-9\-_]+" link))

(defn is-youtube-desktop-link? [link]
   (re-matches #".*youtube.com/watch\?.*v=[a-zA-Z0-9\-_]+.*" link))

(defn is-youtube-link? [link]
  (or (is-youtube-desktop-link? link) (is-youtube-mobile-link? link)))

(defn get-youtube-video-id [link]
  (cond 
   (is-youtube-mobile-link? link) (second (re-matches #".*youtu.be/([a-zA-Z0-9\-_]+)" link))
   (is-youtube-desktop-link? link) (second (re-matches #".*youtube.com/watch\?.*v=([a-zA-Z0-9\-_]+).*" link))))

(defn gen-youtube-embeded-link [video-id]
  (str "http://www.youtube.com/embed/" video-id))

(defn add-related-link [username song-id link]
  (if (is-youtube-link? link)
    (let [video-id (get-youtube-video-id link)
          youtube-embeded-link (gen-youtube-embeded-link video-id)]
      (data/add-song-external-video-link song-id youtube-embeded-link)
      (log/info username "has added a related youtube video(" link ") for song" song-id)
      (redirect (str "/music/" song-id)))
    {:status 400}))

(defn del-related-link [username song-id link]
  (data/del-song-external-video-link song-id link)
  (log/info username "has deleted" link "from the song" song-id))
