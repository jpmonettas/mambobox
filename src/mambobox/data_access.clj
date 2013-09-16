(ns mambobox.data-access
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [clojure.tools.logging :as log]
            [cemerick.friend.credentials :as creds]
            [mambobox.utils :as utils])
  (:use monger.operators
        [mambobox.utils :only [defnlog]])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]
           [java.util Date]))

;; localhost, default port
(mg/connect!)
(mg/set-db! (mg/get-db "mambobox"))



;; Songs

(defn get-all-songs []
  (mq/with-collection "songs"
    (mq/find {})
    (mq/sort (array-map :date-created -1))))

(defn get-all-songs-from-ids [ids-vector]
  (utils/with-auto-object-id [ids-vector]
    (mq/with-collection "songs"
      (mq/find {:_id {$in ids-vector}})
      (mq/sort (array-map :date-created -1)))))
  

(defn get-song-by-id [song-id]
  (utils/with-auto-object-id [song-id]
    (mc/find-one-as-map "songs" {:_id song-id})))

(defn get-song-by-file-name [file-name]
  (mc/find-one-as-map "songs" {:generated-file-name file-name}))



(defn save-song [name artist original-file-name generated-file-name username]
  (mc/insert-and-return "songs" {:_id (ObjectId.) 
                                 :name name 
                                 :artist artist
                                 :original-file-name original-file-name
                                 :uploader-username username
                                 :visits 0
                                 :generated-file-name generated-file-name
                                 :date-created (new Date)}))

(defn track-song-access [song-id]
  (utils/with-auto-object-id [song-id]
    (mc/update "songs" {:_id song-id} {$inc {:visits 1}})))

(defn get-most-visited-songs 
  ([] (get-most-visited-songs 30))
  ([number]
       (mq/with-collection "songs"
         (mq/find)
         (mq/sort (array-map :visits -1))
         (mq/limit number))))
    

;; Song Tags

(defn add-song-tag [song-id tagname]
  (utils/with-auto-object-id [song-id]
    (mc/update "songs" {:_id song-id} {$addToSet {:tags tagname}})))
      
(defn del-song-tag [song-id tagname]
  (utils/with-auto-object-id [song-id]
    (mc/update "songs" {:_id song-id} {$pull {:tags tagname}})))

(defn update-song [song-id song-name artist]
  (utils/with-auto-object-id [song-id]
  (when (not (empty? song-name))
    (mc/update "songs" {:_id song-id} {$set {:name song-name}}))
  (when (not (empty? artist))
    (mc/update "songs" {:_id song-id} {$set {:artist artist}}))
  (get-song-by-id song-id)))


;; Song Video Links

(defn add-song-external-video-link [song-id link]
  (utils/with-auto-object-id [song-id]
    (mc/update "songs" {:_id song-id} {$addToSet {:external-video-links link}})))

(defn del-song-external-video-link [song-id link]
  (utils/with-auto-object-id [song-id]
    (mc/update "songs" {:_id song-id} {$pull {:external-video-links link}})))


;; Users Favourites

(defn add-song-to-favourites [song-id user-id]
  (utils/with-auto-object-id [user-id]
    (mc/update "users" {:_id user-id} {$addToSet {:favourites song-id}})))

(defn del-song-from-favourites [song-id user-id]
  (utils/with-auto-object-id [user-id]
    (mc/update "users" {:_id user-id} {$pull {:favourites song-id}})))

;;Users visited songs

(defn add-song-to-visited [user-id song-id]
  (utils/with-auto-object-id [user-id]
    (mc/update "users" {:_id user-id} {$addToSet {:visited song-id}})))


;; Users

(defn add-user [username plainpass first-name last-name]
  (mc/insert "users" {:_id (ObjectId.) :first-name first-name :last-name last-name :username username :password (creds/hash-bcrypt plainpass)}))
  
(defn get-user-by-username [username]
  (mc/find-one-as-map "users" {:username username}))

(defn get-user-by-id [user-id]
  (utils/with-auto-object-id [user-id]
    (mc/find-one-as-map "users" {:_id user-id})))

(defn create-invitation []
  (mc/insert "invitations" {:number (str (int (rand 10000)))}))

(defn check-invitation [number]
  (mc/find-one-as-map "invitations" {:number number}))

(defn remove-invitation [number]
  (mc/remove "invitations" {:number number}))

(defn get-all-users []
  (mc/find-maps "users"))

;; Notes

(defn add-new [username newtext]
    (mc/insert "news" {:_id (ObjectId.) :username username :text newtext :date-created (new Date)}))

(defn get-all-news []
  (mq/with-collection "news"
    (mq/find {})
    (mq/fields [:username :text :date-created])
    (mq/sort (array-map :date-created -1))))
