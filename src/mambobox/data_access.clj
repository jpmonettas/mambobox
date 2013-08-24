(ns mambobox.data-access
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [clojure.tools.logging :as log]
            [cemerick.friend.credentials :as creds])
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

(defn get-song-by-id [id]
  (mc/find-one-as-map "songs" {:_id (ObjectId. id)}))

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
  (mc/update "songs" {:_id (ObjectId. song-id)} {$inc {:visits 1}}))

;; Song Tags

(defn add-song-tag [song-id tagname]
  (mc/update "songs" {:_id (ObjectId. song-id)} {$addToSet {:tags tagname}}))
      
(defn del-song-tag [song-id tagname]
  (mc/update "songs" {:_id (ObjectId. song-id)} {$pull {:tags tagname}}))

(defn update-song [song-id song-name artist]
  (when (not (empty? song-name))
    (mc/update "songs" {:_id (ObjectId. song-id)} {$set {:name song-name}}))
  (when (not (empty? artist))
    (mc/update "songs" {:_id (ObjectId. song-id)} {$set {:artist artist}}))
  (get-song-by-id song-id))


;; Song Video Links

(defn add-song-external-video-link [song-id link]
  (mc/update "songs" {:_id (ObjectId. song-id)} {$addToSet {:external-video-links link}}))

(defn del-song-external-video-link [song-id link]
  (mc/update "songs" {:_id (ObjectId. song-id)} {$pull {:external-video-links link}}))


;; Users

(defn add-user [username plainpass]
  (mc/insert "users" {:_id (ObjectId.) :username username :password (creds/hash-bcrypt plainpass)}))
  
(defn get-user-by-username [username]
  (mc/find-one-as-map "users" {:username username}))

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
