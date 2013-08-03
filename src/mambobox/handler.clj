(ns mambobox.handler
  (:use compojure.core
        hiccup.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [mambobox.views.home :as views]))



(defroutes app-routes
  (GET "/" [] (views/music-page-view))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
