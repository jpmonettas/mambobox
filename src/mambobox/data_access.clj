(ns mambobox.data-access
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.tools.logging :as log])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))

;; localhost, default port
(mg/connect!)
(mg/set-db! (mg/get-db "mambobox"))

(defn get-all-songs []
  (mc/find-maps "songs"))

(defn get-song-by-id [id]
  (mc/find-one-as-map "songs" {:_id (ObjectId. id)}))

(defn save-song [name artist generated-file-name]
  (mc/insert "songs" {:_id (ObjectId.) 
                      :name name 
                      :artist artist
                      :tags []
                      :generated-file-name generated-file-name}))
  
