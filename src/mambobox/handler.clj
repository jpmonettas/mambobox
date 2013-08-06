(ns mambobox.handler
  (:use [compojure.core]
        [hiccup.core]
        [ring.middleware.params]
        [ring.middleware.multipart-params]
        [ring.adapter.jetty])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [mambobox.controllers.music :as mc]
            [mambobox.controllers.home :as hc]))


(defroutes app-routes
  ;;Music Page
  (GET "/music/" [q curpage] (mc/music-search q curpage))
  (GET "/music/:id" [id] (mc/music-id id))
  (POST "/music/:id" [id songname artist] (mc/edit-music id songname artist))

  (GET "/upload" [] (mc/upload-page))
  (POST "/upload" [files] (mc/upload-file files))

  ;; Home Page
  (GET "/" [] (hc/home))

  ;; Resources
  (route/resources "/")

  ;; Music Files
  (route/files "/files/" {:root "/home/jmonetta/temp/music"}) 

  ;; Not Found
  (route/not-found "Not Found"))

(def app 
  (handler/site app-routes))
