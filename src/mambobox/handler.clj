(ns mambobox.handler
  (:use compojure.core
        hiccup.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [mambobox.controllers.music :as mc]
            [mambobox.views.home :as hv]))



(defroutes app-routes
  ;;Music Page
  (GET "/music/" [] (mc/music-search))
  (GET "/music/:id" [id] (mc/music-id id))
  (POST "/music/:id" [id songname artist] (mc/edit-music id songname artist))

  ;; Home Page
  (GET "/" [] (hv/home-page-view))

  ;; Resources
  (route/resources "/")

  ;; Not Found
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
