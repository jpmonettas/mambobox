(ns mambobox.controllers.music
  (:use [mambobox.views.music-search :only [music-search-view]]
        [mambobox.views.music-detail :only [music-detail-view]]
        [mambobox.views.music-upload :only [music-upload-view]]
        [ring.util.response]
        [slingshot.slingshot :only [throw+ try+]])
  (:require [mambobox.utils :as utils]
            [mambobox.data-access :as data]
            [taoensso.timbre :as log]
            [clojure.data.json :as json]
            [clojure.set :as set]
            [mambobox.services.songs :as ss]
            [mambobox.services.users :as us]))


(defn surprise-me [db-cmp system-config user-id]
  {:io? true}
  (let [user (us/get-user-by-id db-cmp user-id)
        suggeste-scored-percentage (:suggested-score-percentage system-config)
        suggesteds-size (:suggesteds-size system-config)
        surprise-me-vector (into [] (us/get-suggested-songs-for-user user-id suggeste-scored-percentage suggesteds-size))
        random-song-index (int (rand (count surprise-me-vector)))
        song (get surprise-me-vector random-song-index)]
    (log/info "Song" (:name song) "choosed for" (:usename user))
    (redirect (str "/music/" (:_id song)))))
                
(defn music-search [db-cmp system-config user-id q tag collection-filter cur-page]
  {:io? true}
  (let [user (us/get-user-by-id db-cmp user-id)
        username (:username user)
        user-favourite-song-ids (:favourites user)
        collection-filter (if collection-filter collection-filter "all")
        cur-page (if cur-page (utils/parse-int cur-page) 1)
        base-collection (cond (= collection-filter "all") (ss/get-all-songs db-cmp)
                              (= collection-filter "favourites") (when-not (empty? user-favourite-song-ids)
                                                                   (ss/get-all-songs db-cmp user-favourite-song-ids)))
        search-result (ss/search-music q tag base-collection)
        tags-freaquency-map (ss/get-tags-freaquency-map search-result)
        num-pages (ss/get-cant-pages search-result (:result-page-size system-config))
        cur-page-songs (ss/get-collection-page search-result cur-page (:result-page-size system-config))]
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



(defn music-id [db-cmp user-id song-id]
  {:io? true}
  (let [song (ss/get-song-by-id db-cmp song-id)
        user (us/get-user-by-id db-cmp user-id)
        username (:username user)
        selected-suggesteds-songs (us/get-suggested-songs-for-user user-id)]
    (ss/track-song-access db-cmp song-id)
    (us/add-song-to-visited db-cmp user-id song-id)
    (log/info username "seeing" (:name song) "[" song-id "]")
    (music-detail-view username song (us/is-song-user-favourite? song-id user) selected-suggesteds-songs)))

(defn edit-music [db-cmp user-id song-id song-name artist]
  {:io? true} 
  (let [user (us/get-user-by-id db-cmp user-id)
        username (:username user)
        song (ss/update-song db-cmp song-id song-name artist)
        selected-suggesteds-songs (us/get-suggested-songs-for-user user-id)]
    (log/info username "editing song [" song-id "] with new name : [" song-name "] and new artist : [" artist "]")
    (music-detail-view username song (us/is-song-user-favourite? song-id user) selected-suggesteds-songs)))

(defn upload-page [username]
  (music-upload-view username))

(defn upload-file [db-cmp system-config username file]
  {:io? true}
  (json/write (ss/upload-file db-cmp system-config username file)))

(defn add-tag [db-cmp username song-id tag-name]
  {:io? true}
  (ss/add-song-tag db-cmp song-id tag-name)
  (log/info username "has tagged" song-id "with" tag-name)
  {:status 204})

(defn delete-tag [db-cmp username song-id tag-name]
  {:io? true}
  (ss/del-song-tag db-cmp song-id tag-name)
  (log/info username "has deleted from" song-id "tag" tag-name)
  {:status 204})


(defn add-related-link [db-cmp username song-id link]
  {:io? true}
  (if (ss/is-youtube-link? link)
    (let [video-id (ss/get-youtube-video-id link)
          youtube-embeded-link (ss/gen-youtube-embeded-link video-id)]
      (ss/add-song-external-video-link db-cmp song-id youtube-embeded-link)
      (log/info username "has added a related youtube video(" link ") for song" song-id)
      (redirect (str "/music/" song-id)))
    {:status 400}))

(defn del-related-link [db-cmp username song-id link]
  {:io? true}
  (ss/del-song-external-video-link db-cmp song-id link)
  (log/info username "has deleted" link "from the song" song-id)
  {:status 204})
