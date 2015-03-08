(ns mambobox.controllers.home
  (:use [mambobox.views.home :only [home-page-view]]
        [ring.util.response])
  (:require [mambobox.data-access :as data]
            [taoensso.timbre :as log]))

(defn home [db-cmp username]
  (let [news-to-show (data/get-all-news db-cmp)]
    (home-page-view username news-to-show)))

(defn add-new [db-cmp username newtext]
  (log/info username "added a new with text:" newtext)
  (data/add-new db-cmp username newtext)
  (redirect "/"))
