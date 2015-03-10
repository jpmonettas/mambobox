(ns mambobox.controllers.users
  (:use mambobox.views.user-creation
        mambobox.views.login)
  (:require [mambobox.services.users :as us]
            [taoensso.timbre :as log]
            [clj-time.core :refer [ago]]))

(defn add-song-to-favourites [db-cmp song-id user-id username]
  (us/add-song-to-favourites db-cmp song-id user-id)
  (log/info "User" username "[" user-id "]" "has added" song-id "to favourites")
  {:status 200})

(defn del-song-from-favourites [db-cmp song-id user-id]
  (us/del-song-from-favourites db-cmp song-id user-id)
  {:status 204})

(defn create-user [db-cmp first-name last-name username password password2 invitation]
  (let [errors
        (hash-set
         (when (empty? first-name) :first-name)
         (when (empty? last-name) :last-name)
         (when (or (empty? username)
                   (not (re-seq #".+@.+\..+" username))) :username)
         (when (us/get-user-by-username db-cmp username) :username-exist)
         (when (or (empty? password)
                   (not (= password password2))) :password)
         (when (or (empty? invitation)
                  (not (us/check-invitation db-cmp invitation))) :invitation))
        continue (and (= (count errors) 1) (nil? (first errors)))]
    (if continue
      (do
        (log/info "Created user" username "first name :" first-name "last name:" last-name "invitation:" invitation)
        (us/add-user db-cmp username password first-name last-name)
        (us/remove-invitation db-cmp invitation)
        (login username))
      (create-user-view errors first-name last-name username))))
