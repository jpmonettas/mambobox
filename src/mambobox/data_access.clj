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

(defn get-all-songs []
  (mc/find-maps "songs"))

(defn get-song-by-id [id]
  (mc/find-one-as-map "songs" {:_id (ObjectId. id)}))

(defn save-song [name artist original-file-name generated-file-name]
  (mc/insert "songs" {:_id (ObjectId.) 
                      :name name 
                      :artist artist
                      :tags []
                      :original-file-name original-file-name
                      :generated-file-name generated-file-name}))
  

(defn add-song-tag [song-id tagname]
  (mc/update "songs" {:_id (ObjectId. song-id)} {$push {:tags tagname}}))
      
