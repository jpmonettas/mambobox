(ns mambobox.controllers.music
  (:use [mambobox.views.music-search :only [music-search-view]]
        [mambobox.views.music-detail :only [music-detail-view]]
        [mambobox.views.music-upload :only [music-upload-view]]
        [ring.util.response]
        [mambobox.utils :only [defnlog dlet]]
        [clojure.string :only [lower-case]])
  (:require [mambobox.utils :as utils]
            [mambobox.data-access :as data]
            [clojure.tools.logging :as log]
            [fuzzy-string.core :as fuzz-str]
            [clojure.data.json :as json]))


(defn accept-song-for-query? [song qstring]
  (let [song-name (lower-case (get song :name))
        song-artist (lower-case (get song :artist))]                              
    (or (> (fuzz-str/dice song-name qstring) 0.5) 
        (> (fuzz-str/dice song-artist qstring) 0.5)))) 
                          
(defn song-contains-tag? [song tag]
  (let [tags (get song :tags)]
    (some #{tag} tags)))


(def page-size 10)

(defn music-search [username q tag cur-page]
  (dlet [processed-q (when q (lower-case q))
        all-songs (data/get-all-songs)
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
        cur-page (if cur-page
                   (utils/parse-int cur-page)
                   1)
        cant-filtered-songs (count tag-filtered-songs)
        num-pages (+ (quot cant-filtered-songs page-size) 
                     (if (> (mod cant-filtered-songs page-size) 0) 1 0))
        cur-page-songs (let [first-song (inc (* (dec cur-page) page-size))
                             last-song (dec (+ first-song page-size))]
                         (log/debug "Cutting the list from " first-song " to " last-song)
                         (utils/sub-list tag-filtered-songs first-song last-song))]
    (log/debug "We have " cant-filtered-songs " songs after filtering.")
    (log/debug "We are going to retrieve page " cur-page " of " num-pages) 
    (music-search-view username cur-page-songs q tag cur-page num-pages)))

(defn music-id [username id]
  (let [song (data/get-song-by-id id)]
    (data/track-song-access id)
    (music-detail-view username song)))

(defn edit-music [username id song-name artist] 
  (let [song (data/update-song id song-name artist)]
    (music-detail-view username song)))

(defn upload-page [username]
  (music-upload-view username))

(def upload-dir "/home/jmonetta/temp/music/")

(defnlog upload-file [username file]
  (let [file-map (first file)
        file-name (file-map :filename)
        temp-file (file-map :tempfile)
        size (file-map :size)
        generated-file-name (utils/gen-uuid temp-file)
        metadata (utils/get-metadata temp-file)
        metadata-tags (when metadata
                        (metadata :tags))
        title-tag (when metadata-tags
                    (first (metadata-tags :title)))
        artist-tag (when metadata-tags
                     (first (metadata-tags :artist)))
        song-name (if (not (empty? title-tag)) title-tag file-name)
        song-artist (if (not (empty? artist-tag)) artist-tag "Desconocido")]
    (utils/save-file-to-disk file-map generated-file-name upload-dir)
    (let [created-song (data/save-song song-name
                                      song-artist
                                      file-name
                                      generated-file-name username)] 
      (json/write-str {:files [{:name file-name
                                :size size
                                :url (str "/music/" (get created-song :_id))}]}))))



(defn add-tag [username song-id tag-name]
  (data/add-song-tag song-id tag-name)
  {:status 200})

(defn delete-tag [username song-id tag-name]
  (data/del-song-tag song-id tag-name)
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
      (redirect (str "/music/" song-id)))
    {:status 400}))

(defn del-related-link [username song-id link]
  (data/del-song-external-video-link song-id link))
