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


;; (with-auto-object-id [user-id]
;;     (mc/update "users" {:_id user-id} {$addToSet {:visited song-id}}))

;; Macroexpands to ->

;; (let [user-id (if (instance? java.lang.String user-id)
;;                 (ObjectId. user-id)
;;                 user-id)]
;;   (mc/update "users" {:_id user-id} {$addToSet {:visited song-id}}))

(defmacro with-auto-object-id [ids & forms]
  `(let 
       ~(into []
              (reduce 
               concat
               (map
                (fn [bind-name]
                  [bind-name `(if (vector? ~bind-name)
                                (if (instance? String (first ~bind-name))
                                  (map #(ObjectId. %) ~bind-name)
                                  ~bind-name)
                                (if (instance? String ~bind-name)
                                  (ObjectId. ~bind-name)
                                  ~bind-name))])
                ids)))
     ~@forms))


;; Songs

(defn get-all-songs []
  (mq/with-collection "songs"
    (mq/find {})
    (mq/sort (array-map :date-created -1))))

(defn get-all-songs-from-ids [ids-vector]
  (with-auto-object-id [ids-vector]
    (mq/with-collection "songs"
      (mq/find {:_id {$in ids-vector}})
      (mq/sort (array-map :date-created -1)))))
  

(defn get-song-by-id [song-id]
  (with-auto-object-id [song-id]
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
  (with-auto-object-id [song-id]
    (mc/update "songs" {:_id song-id} {$inc {:visits 1}})))

;; Song Tags

(defn add-song-tag [song-id tagname]
  (with-auto-object-id [song-id]
    (mc/update "songs" {:_id song-id} {$addToSet {:tags tagname}})))
      
(defn del-song-tag [song-id tagname]
  (with-auto-object-id [song-id]
    (mc/update "songs" {:_id song-id} {$pull {:tags tagname}})))

(defn update-song [song-id song-name artist]
  (with-auto-object-id [song-id]
  (when (not (empty? song-name))
    (mc/update "songs" {:_id song-id} {$set {:name song-name}}))
  (when (not (empty? artist))
    (mc/update "songs" {:_id song-id} {$set {:artist artist}}))
  (get-song-by-id song-id)))


;; Song Video Links

(defn add-song-external-video-link [song-id link]
  (with-auto-object-id [song-id]
    (mc/update "songs" {:_id song-id} {$addToSet {:external-video-links link}})))

(defn del-song-external-video-link [song-id link]
  (with-auto-object-id [song-id]
    (mc/update "songs" {:_id song-id} {$pull {:external-video-links link}})))


;; Users Favourites

(defn add-song-to-favourites [song-id user-id]
  (with-auto-object-id [user-id]
    (mc/update "users" {:_id user-id} {$addToSet {:favourites song-id}})))

(defn del-song-from-favourites [song-id user-id]
  (with-auto-object-id [user-id]
    (mc/update "users" {:_id user-id} {$pull {:favourites song-id}})))

;;Users visited songs

(defn add-song-to-visited [user-id song-id]
  (with-auto-object-id [user-id]
    (mc/update "users" {:_id user-id} {$addToSet {:visited song-id}})))


;; Users

(defn add-user [username plainpass]
  (mc/insert "users" {:_id (ObjectId.) :username username :password (creds/hash-bcrypt plainpass)}))
  
(defn get-user-by-username [username]
  (mc/find-one-as-map "users" {:username username}))

(defn get-user-by-id [user-id]
  (with-auto-object-id [user-id]
    (mc/find-one-as-map "users" {:_id user-id})))

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
