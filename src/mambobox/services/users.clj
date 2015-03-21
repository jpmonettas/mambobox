(ns mambobox.services.users
  (:require [mambobox.utils :as utils]
            [mambobox.data-access :as data]
            [taoensso.timbre :as l]
            [clj-time.core :refer [weeks ago after?]]
            [mambobox.services.songs :as ss]))

(defn get-all-favourites [users]
  (into #{} (reduce concat (map #(:favourites %) users))))

(defn get-user-favourites-songs [db-cmp user-id]
  (->> (data/get-user-by-id db-cmp user-id)
     :favourites
     (data/get-all-songs-from-ids db-cmp)))
   
(defn get-suggested-songs-for-user [db-cmp user-id suggeste-scored-percentage suggesteds-size]
  {:io? true}
  (let [all-songs (data/get-all-songs db-cmp)
        user (data/get-user-by-id db-cmp user-id)
        user-favs-ids (:favourites user)
        all-users (data/get-all-users db-cmp)
        all-favourites-ids (get-all-favourites all-users)
        newer-bottom-limit (-> 3 weeks ago)]
    (->
     (ss/make-scored-songs-col all-songs user-favs-ids all-favourites-ids newer-bottom-limit)
     (ss/make-top-suggested-songs-col suggeste-scored-percentage)
     (utils/make-random-subset suggesteds-size))))


(defn is-song-user-favourite? [song-id user]
  (let [favourites (:favourites user)]
        (some #{(.toString song-id)} favourites)))


(defn get-user-by-id [db-cmp user-id]
  (data/get-user-by-id db-cmp user-id))

(defn get-user-by-username [db-cmp username]
  (data/get-user-by-username db-cmp username))

(defn add-song-to-visited [db-cmp user-id song-id]
  (data/add-song-to-visited db-cmp user-id song-id))

(defn add-song-to-favourites [db-cmp song-id user-id]
  (data/add-song-to-favourites db-cmp song-id user-id))

(defn del-song-from-favourites [db-cmp song-id user-id]
  (data/del-song-from-favourites db-cmp song-id user-id))

(defn promote-user [db-cmp user-id]
  (data/update-user-role db-cmp user-id :admin-user))

(defn demote-user [db-cmp user-id]
  (data/update-user-role db-cmp user-id :normal-user))

(defn check-invitation [db-cmp invitation]
  (data/check-invitation db-cmp invitation))

(defn add-user [db-cmp username password first-name last-name]
  (data/add-user db-cmp username password first-name last-name))

(defn remove-invitation [db-cmp invitation]
  (data/remove-invitation db-cmp invitation))
