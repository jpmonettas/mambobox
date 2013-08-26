(ns mambobox.controllers.home
  (:use [mambobox.views.home :only [home-page-view]]
        [ring.util.response])
  (:require [mambobox.data-access :as data]
            [clojure.tools.logging :as log]))

(defn home [username]
  (let [news-to-show (data/get-all-news)]
    (home-page-view username news-to-show)))

(defn add-new [username newtext]
  (log/info username "added a new with text:" newtext)
  (data/add-new username newtext)
  (redirect "/"))
