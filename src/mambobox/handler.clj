(ns mambobox.handler
  (:use compojure.core
        hiccup.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [mambobox.views.views :as views]))



(defroutes app-routes
  (GET "/" [] (views/test-view-in-views))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
