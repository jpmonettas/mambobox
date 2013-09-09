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


(defn get-tags-freaquency-map [songs]
  (reduce (fn [map next] 
            (assoc map next (inc (get map next 0)))) 
          {}
          (reduce concat (map #(:tags %) songs))))


(defn accept-song-for-query? [song qstring]
  (let [song-name (lower-case (get song :name))
        song-artist (lower-case (get song :artist))]                              
    (or (> (fuzz-str/dice song-name qstring) 0.5) 
        (> (fuzz-str/dice song-artist qstring) 0.5)
        (and (> (count qstring) 3)
             (or
              (utils/str-contains song-name qstring)
              (utils/str-contains song-artist qstring)))))) 
                          
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
        

(defn search-music [q tag all-songs]
  (dlet [processed-q (when q (lower-case q))
        query-filtered-songs (if (not (empty? q)) 
                               (filter (fn [song]
                                         (accept-song-for-query? song processed-q))
                                       all-songs)
                               all-songs)
        tag-filtered-songs (if (not (empty? tag)) 
                             (filter (fn [song]
                                       (song-contains-tag? song tag))
                                     query-filtered-songs)
                             query-filtered-songs)]
        tag-filtered-songs))

(defn music-search [user-id q tag collection-filter cur-page]
  (dlet [user (data/get-user-by-id user-id)
        username (:username user)
        user-favourite-song-ids (:favourites user)
        collection-filter (if collection-filter collection-filter "all")
        cur-page (if cur-page (utils/parse-int cur-page) 1)
        base-collection (cond (= collection-filter "all") (data/get-all-songs)
                              (= collection-filter "favourites") (when-not (empty? user-favourite-song-ids)
                                                                   (data/get-all-songs-from-ids user-favourite-song-ids)))
        search-result (search-music q tag base-collection)
        tags-freaquency-map (get-tags-freaquency-map search-result)
        num-pages (get-cant-pages search-result  config/result-page-size)
        cur-page-songs (get-collection-page search-result cur-page config/result-page-size)]
    (log/info username "searching for [" q "] with tag [" tag "] and page" cur-page "retrieved" (count search-result) "songs")
    (music-search-view username
                       cur-page-songs
                       q
                       tag
                       collection-filter
                       cur-page
                       num-pages
                       tags-freaquency-map
                       (= collection-filter "favourites"))))

(defn is-song-user-favourite? [song-id user]
  (let [favourites (:favourites user)]
        (some #{(.toString song-id)} favourites)))

(defn music-id [user-id song-id]
  (let [song (data/get-song-by-id song-id)
        user (data/get-user-by-id user-id)
        username (:username user)]
    (data/track-song-access song-id)
    (data/add-song-to-visited user-id song-id)
    (log/info username "seeing" (:name song) "[" song-id "]")
    (music-detail-view username song (is-song-user-favourite? song-id user))))

(defn edit-music [user-id song-id song-name artist] 
  (let [user (data/get-user-by-id user-id)
        username (:username user)
        song (data/update-song song-id song-name artist)]
    (log/info username "editing song [" song-id "] with new name : [" song-name "] and new artist : [" artist "]")
    (music-detail-view username song (is-song-user-favourite? song-id user))))

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
  {:status 204})

(defn delete-tag [username song-id tag-name]
  (data/del-song-tag song-id tag-name)
  (log/info username "has deleted from" song-id "tag" tag-name)
  {:status 204})

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
  (log/info username "has deleted" link "from the song" song-id)
  {:status 204})
