(ns mambobox.data-access
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.tools.logging :as log])
  (:use monger.operators)
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

(defn save-song [name artist original-file-name generated-file-name]
  (mc/insert "songs" {:_id (ObjectId.) 
                      :name name 
                      :artist artist
                      :original-file-name original-file-name
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
