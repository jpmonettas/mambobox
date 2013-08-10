(ns mambobox.controllers.music
  (:use [mambobox.views.music-search :only [music-search-view]]
        [mambobox.views.music-detail :only [music-detail-view]]
        [mambobox.views.music-upload :only [music-upload-view]]
        [clojure.string :only [lower-case]])
  (:require [mambobox.utils :as utils]
            [mambobox.data-access :as data]
            [clojure.tools.logging :as log]))


(defn song-contains-qstring? [song qstring]
    (or (utils/str-contains (lower-case (get song :name)) qstring)
        (utils/str-contains (lower-case (get song :artist)) qstring)))

(defn song-contains-tag? [song tag]
  (let [tags (get song :tags)]
    (some #{tag} tags)))


(def page-size 10)

(defn music-search [q tag cur-page]
  (let [processed-q (when q (lower-case q))
        all-songs (data/get-all-songs)
        query-filtered-songs (if (not (empty? q)) 
                               (filter (fn [song]
                                         (song-contains-qstring? song processed-q))
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
    (music-search-view cur-page-songs q tag cur-page num-pages)))

(defn music-id [id]
  (let [song (data/get-song-by-id id)]
    (music-detail-view song)))

(defn edit-music [id song-name artist] 
  (let [song (data/update-song id song-name artist)]
    (music-detail-view song)))

(defn upload-page []
  (music-upload-view))

(def upload-dir "/home/jmonetta/temp/music/")

(defn upload-file [file]
  (let [new-file-name (utils/gen-uuid)
        file-name (file :filename)
        size (utils/save-file-to-disk file new-file-name upload-dir)
        song-name file-name]
    (data/save-song song-name "Unknown" file-name new-file-name)
    {:status 200
       :size size
       :headers {"Content-Type" "text/html"}}))

(defn add-tag [song-id tag-name]
  (data/add-song-tag song-id tag-name)
  {:status 200})

(defn delete-tag [song-id tag-name]
  (data/del-song-tag song-id tag-name)
  {:status 200})

