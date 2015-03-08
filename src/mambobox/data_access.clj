(ns mambobox.data-access
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [taoensso.timbre :as l]
            [cemerick.friend.credentials :as creds]
            [mambobox.utils :as utils]
            [mambobox.components.db :as db])
  (:use monger.operators
        [mambobox.utils :only [defnlog]])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]
           [java.util Date]))


;; Songs

(defn get-all-songs [db-cmp]
  (mq/with-collection (db/get-db db-cmp) "songs"
    (mq/find {})
    (mq/sort (array-map :date-created -1))))

(defn get-all-songs-from-ids [db-cmp ids-vector]
  (utils/with-auto-object-id [ids-vector]
    (mq/with-collection (db/get-db db-cmp) "songs"
      (mq/find {:_id {$in ids-vector}})
      (mq/sort (array-map :date-created -1)))))
  

(defn get-song-by-id [db-cmp song-id]
  (utils/with-auto-object-id [song-id]
    (mc/find-one-as-map (db/get-db db-cmp) "songs" {:_id song-id})))

(defn get-song-by-file-name [db-cmp file-name]
  (mc/find-one-as-map (db/get-db db-cmp) "songs" {:generated-file-name file-name}))


(defn save-song [db-cmp name artist original-file-name generated-file-name username]
  (mc/insert-and-return (db/get-db db-cmp) "songs" {:_id (ObjectId.) 
                                 :name name 
                                 :artist artist
                                 :original-file-name original-file-name
                                 :uploader-username username
                                 :visits 0
                                 :generated-file-name generated-file-name
                                 :date-created (new Date)}))

(defn track-song-access [db-cmp song-id]
  (utils/with-auto-object-id [song-id]
    (mc/update (db/get-db db-cmp) "songs" {:_id song-id} {$inc {:visits 1}})))

(defn get-most-visited-songs 
  ([db-cmp] (get-most-visited-songs db-cmp 30))
  ([db-cmp number]
       (mq/with-collection (db/get-db db-cmp) "songs"
         (mq/find)
         (mq/sort (array-map :visits -1))
         (mq/limit number))))
    

;; Song Tags

(defn add-song-tag [db-cmp song-id tagname]
  (utils/with-auto-object-id [song-id]
    (mc/update (db/get-db db-cmp) "songs" {:_id song-id} {$addToSet {:tags tagname}})))
      
(defn del-song-tag [db-cmp song-id tagname]
  (utils/with-auto-object-id [song-id]
    (mc/update (db/get-db db-cmp) "songs" {:_id song-id} {$pull {:tags tagname}})))

(defn update-song [db-cmp song-id song-name artist]
  (utils/with-auto-object-id [song-id]
  (when (not (empty? song-name))
    (mc/update (db/get-db db-cmp) "songs" {:_id song-id} {$set {:name song-name}}))
  (when (not (empty? artist))
    (mc/update (db/get-db db-cmp) "songs" {:_id song-id} {$set {:artist artist}}))
  (get-song-by-id song-id)))


;; Song Video Links

(defn add-song-external-video-link [db-cmp song-id link]
  (utils/with-auto-object-id [song-id]
    (mc/update (db/get-db db-cmp) "songs" {:_id song-id} {$addToSet {:external-video-links link}})))

(defn del-song-external-video-link [db-cmp song-id link]
  (utils/with-auto-object-id [song-id]
    (mc/update (db/get-db db-cmp) "songs" {:_id song-id} {$pull {:external-video-links link}})))


;; Users Favourites

(defn add-song-to-favourites [db-cmp song-id user-id]
  (utils/with-auto-object-id [user-id]
    (mc/update (db/get-db db-cmp) "users" {:_id user-id} {$addToSet {:favourites song-id}})))

(defn del-song-from-favourites [db-cmp song-id user-id]
  (utils/with-auto-object-id [user-id]
    (mc/update (db/get-db db-cmp) "users" {:_id user-id} {$pull {:favourites song-id}})))

;;Users visited songs

(defn add-song-to-visited [db-cmp user-id song-id]
  (utils/with-auto-object-id [user-id]
    (mc/update (db/get-db db-cmp) "users" {:_id user-id} {$addToSet {:visited song-id}})))


;; Users

(defn add-user [db-cmp username plainpass first-name last-name]
  (mc/insert (db/get-db db-cmp) "users" {:_id (ObjectId.) :first-name first-name :last-name last-name :username username :password (creds/hash-bcrypt plainpass)}))
  
(defn get-user-by-username [db-cmp username]
  (l/info "Submitted username " username)
  (mc/find-one-as-map (db/get-db db-cmp) "users" {:username username}))

(defn get-user-by-id [db-cmp user-id]
  (utils/with-auto-object-id [user-id]
    (mc/find-one-as-map (db/get-db db-cmp) "users" {:_id user-id})))

(defn create-invitation [db-cmp]
  (mc/insert (db/get-db db-cmp) "invitations" {:number (str (int (rand 10000)))}))

(defn check-invitation [db-cmp number]
  (mc/find-one-as-map (db/get-db db-cmp) "invitations" {:number number}))

(defn remove-invitation [db-cmp number]
  (mc/remove (db/get-db db-cmp) "invitations" {:number number}))

(defn get-all-users [db-cmp]
  (mc/find-maps (db/get-db db-cmp) "users"))

;; Notes

(defn add-new [db-cmp username newtext]
    (mc/insert (db/get-db db-cmp) "news" {:_id (ObjectId.) :username username :text newtext :date-created (new Date)}))

(defn get-all-news [db-cmp]
  (mq/with-collection (db/get-db db-cmp) "news"
    (mq/find {})
    (mq/fields [:username :text :date-created])
    (mq/sort (array-map :date-created -1))))
