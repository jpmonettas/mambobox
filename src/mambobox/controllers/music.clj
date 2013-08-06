(ns mambobox.controllers.music
  (:use [mambobox.views.music-search :only [music-search-view]]
        [mambobox.views.music-detail :only [music-detail-view]]
        [mambobox.views.music-upload :only [music-upload-view]]
        [clojure.string :only [lower-case]])
  (:require [mambobox.utils :as utils]
            [mambobox.data-access :as data]
            [clojure.tools.logging :as log]))


(defn song-contains-qstring? [qstring]
  (fn [song]
    (or (utils/str-contains (lower-case (get song :name)) qstring)
        (utils/str-contains (lower-case (get song :artist)) qstring))))

(def page-size 10)

(defn music-search [q cur-page]
  (let [processed-q (when q (lower-case q))
        all-songs (data/get-all-songs)
        filtered-songs (if q 
                         (filter (song-contains-qstring? processed-q) all-songs)
                         all-songs)
        cur-page (if cur-page
                   (utils/parse-int cur-page)
                   1)
        cant-filtered-songs (count filtered-songs)
        num-pages (+ (quot cant-filtered-songs page-size) 
                      (if (> (mod cant-filtered-songs page-size) 0) 1 0))
        cur-page-songs (let [first-song (inc (* (dec cur-page) page-size))
                             last-song (dec (+ first-song page-size))]
                         (log/debug "Cutting the list from " first-song " to " last-song)
                         (utils/sub-list filtered-songs first-song last-song))]
    (log/debug "We have " cant-filtered-songs " songs after filtering.")
    (log/debug "We are going to retrieve page " cur-page " of " num-pages) 
    (music-search-view cur-page-songs q cur-page num-pages)))

(defn music-id [id]
  (let [song (data/get-song-by-id id)]
    (music-detail-view song)))

(defn edit-music [id song-name artist])

(defn upload-page []
  (music-upload-view))

(def upload-dir "/home/jmonetta/temp/music/")

(defn upload-file [file]
  (let [new-file-name (utils/gen-uuid)
        file-name (file :filename)
        size (utils/save-file-to-disk file new-file-name upload-dir)]
    (data/save-song file-name "Unknown" new-file-name)
    {:status 200
       :size size
       :headers {"Content-Type" "text/html"}}))

