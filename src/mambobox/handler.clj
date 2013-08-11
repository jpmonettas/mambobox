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
  (GET "/music/" [q curpage tagfilter] (mc/music-search q tagfilter curpage))
  (GET "/music/:id" [id] (mc/music-id id))
  (POST "/music/:id" [id newsongname newartist] (mc/edit-music id newsongname newartist))
  (POST "/music/:musicid/tags/:tagname" [musicid tagname] (mc/add-tag musicid tagname))
  (DELETE "/music/:musicid/tags/:tagname" [musicid tagname] (mc/delete-tag musicid tagname))

  (POST "/music/:musicid/links/" [musicid newlink] (mc/add-related-link musicid newlink))
  (DELETE "/music/:musicid/links/:link" [musicid link] (mc/del-related-link musicid link))

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
