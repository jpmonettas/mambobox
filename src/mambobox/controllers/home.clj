(ns mambobox.controllers.home
  (:use [mambobox.views.home :only [home-page-view]]))

(defn home []
  (home-page-view))

