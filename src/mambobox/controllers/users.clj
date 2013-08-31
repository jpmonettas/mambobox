(ns mambobox.controllers.users
  (:require [mambobox.data-access :as data]
            [clojure.tools.logging :as log]))

(defn add-song-to-favourites [song-id user-id username]
  (data/add-song-to-favourites song-id user-id)
  (log/info "User" username "[" user-id "]" "has added" song-id "to favourites")
  {:status 200})

(defn del-song-from-favourites [song-id user-id]
  (data/del-song-from-favourites song-id user-id)
  {:status 204})
