(ns mambobox.data-access
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.tools.logging :as log]
            [cemerick.friend.credentials :as creds])
  (:use monger.operators
        [mambobox.utils :only [defnlog]])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))

;; localhost, default port
(mg/connect!)
(mg/set-db! (mg/get-db "mambobox"))


;; Songs

(defn get-all-songs []
  (mc/find-maps "songs"))

(defn get-song-by-id [id]
  (mc/find-one-as-map "songs" {:_id (ObjectId. id)}))

(defnlog save-song [name artist original-file-name generated-file-name username]
  (mc/insert "songs" {:_id (ObjectId.) 
                      :name name 
                      :artist artist
                      :original-file-name original-file-name
                      :uploader-username username
                      :generated-file-name generated-file-name}))

;; Song Tags

(defn add-song-tag [song-id tagname]
  (mc/update "songs" {:_id (ObjectId. song-id)} {$addToSet {:tags tagname}}))
      
(defn del-song-tag [song-id tagname]
  (mc/update "songs" {:_id (ObjectId. song-id)} {$pull {:tags tagname}}))

(defn update-song [song-id song-name artist]
  (mc/update "songs" {:_id (ObjectId. song-id)} {$set {:name song-name :artist artist}})
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
  (mc/insert "news" {:_id (ObjectId.) :username username :text newtext}))

(defn get-all-news []
  (mc/find-maps "news"))
