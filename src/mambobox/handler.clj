(ns mambobox.handler
  (:use [compojure.core]
        [hiccup.core])
  (:require [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [ring.adapter.jetty :as jetty]
            [mambobox.controllers.music :as mc]
            [mambobox.controllers.home :as hc]
            [mambobox.views.login :as lv]
            [mambobox.data-access :as data]
            [mambobox.utils :as utils]
            [mambobox.config])
  (:gen-class))


(defn current-username [req]
  (let [current-ident (friend/current-authentication req)
        username (get current-ident :username)]
    username))
                                     

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;            Secured Routes            ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes app-auth-routes
  ;;Music Page
  (GET "/music/" [q curpage tagfilter :as req] (mc/music-search (current-username req) q tagfilter curpage))
  (GET "/music/:id" [id :as req] (mc/music-id (current-username req) id))
  (POST "/music/:id" [id newsongname newartist :as req] (mc/edit-music (current-username req) id newsongname newartist))
  (POST "/music/:musicid/tags/:tagname" [musicid tagname :as req] (mc/add-tag (current-username req) musicid tagname))
  (DELETE "/music/:musicid/tags/:tagname" [musicid tagname :as req] (mc/delete-tag (current-username req) musicid tagname))

  (POST "/music/:musicid/links/" [musicid newlink :as req] (mc/add-related-link (current-username req) musicid newlink))
  (DELETE "/music/:musicid/links/:link" [musicid link :as req] (mc/del-related-link (current-username req) musicid link))

  (GET "/upload" [:as req] (mc/upload-page (current-username req)))
  (POST "/upload" [files :as req] (mc/upload-file (current-username req) files))

  ;; Home Page
  (GET "/" [:as req] (hc/home (current-username req)))

  ;; News
  (POST "/news/" [newtext :as req] (hc/add-new (current-username req) newtext)) 

  ;; Logout
  (GET "/logout" req
    (friend/logout* (resp/redirect (str (:context req) "/"))))

  ;; Music Files
  (route/files "/files/" {:root mambobox.config/music-dir}) 

  ;; Not Found
  (route/not-found "Not Found"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;        UNSecured Routes              ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes app-routes
 
  ;; Login Page
  (GET "/login" [] (lv/login))

  (GET "/favicon.ico" [] (resp/redirect "/"))

  ;; Resources
  (route/resources "/"))

    
(def app 
  (handler/site 
   (routes
    app-routes
    (friend/authenticate app-auth-routes
                         {:allow-anon? nil
                          :login-uri "/login"
                          :credential-fn (partial creds/bcrypt-credential-fn data/get-user-by-username)
                          :workflows [(workflows/interactive-form)]}))
   {:multipart {:store @utils/my-default-store}}))
    
(defn -main [& [port]]
  (let [port (Integer. (or port
                           (System/getenv "PORT")
                           80))]
     (jetty/run-jetty #'app {:port  port
                            :join? false})))
