(ns mambobox.controllers.music
  (:use [mambobox.views.music-search :only [music-search-view]]
        [mambobox.views.music-detail :only [music-detail-view]]
        [mambobox.views.music-upload :only [music-upload-view]]
        [ring.util.response]
        [mambobox.utils :only [defnlog]]
        [clojure.string :only [lower-case]])
  (:require [mambobox.utils :as utils]
            [mambobox.data-access :as data]
            [clojure.tools.logging :as log]
            [fuzzy-string.core :as fuzz-str]))


(defn accept-song-for-query? [song qstring]
  (let [song-name (lower-case (get song :name))
        song-artist (lower-case (get song :artist))]                              
    (or (> (fuzz-str/dice song-name qstring) 0.5) 
        (> (fuzz-str/dice song-artist qstring) 0.5)))) 
                          
(defn song-contains-qstring? [song qstring]
    (or (utils/str-contains (lower-case (get song :name)) qstring)
        (utils/str-contains (lower-case (get song :artist)) qstring)))

(defn song-contains-tag? [song tag]
  (let [tags (get song :tags)]
    (some #{tag} tags)))


(def page-size 10)

(defn music-search [username q tag cur-page]
  (let [processed-q (when q (lower-case q))
        all-songs (data/get-all-songs)
        query-filtered-songs (if (not (empty? q)) 
                               (filter (fn [song]
                                         ;;(song-contains-qstring? song processed-q))
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
    (music-detail-view username song)))

(defn edit-music [username id song-name artist] 
  (let [song (data/update-song id song-name artist)]
    (music-detail-view song)))

(defn upload-page [username]
  (music-upload-view username))

(def upload-dir "/home/jmonetta/temp/music/")

(defnlog upload-file [username file]
  (let [new-file-name (utils/gen-uuid)
        file-map (first file)
        file-name (file-map :filename)
        temp-file (file-map :tempfile)
        metadata (utils/get-metadata temp-file)
        metadata-tags (metadata :tags)
        title-tag (first (metadata-tags :title))
        artist-tag (first (metadata-tags :artist))
        size (utils/save-file-to-disk file-map new-file-name upload-dir)
        song-name (if (not (empty? title-tag)) title-tag file-name)
        song-artist (if (not (empty? artist-tag)) artist-tag "Desconocido")]
    (data/save-song song-name song-artist file-name new-file-name username)
    {:status 200
       :size size
       :headers {"Content-Type" "text/html"}}))

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
