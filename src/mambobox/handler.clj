(ns mambobox.handler
  (:use [compojure.core]
        [hiccup.core]
        [clojure.tools.nrepl.server :only (start-server stop-server)])
  (:require [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.file :as rmf]
            [mambobox.controllers.music :as mc]
            [mambobox.controllers.users :as uc]
            [mambobox.controllers.home :as hc]
            [mambobox.views.login :as lv]
            [mambobox.views.user-creation :as ucv]
            [mambobox.data-access :as data]
            [mambobox.utils :as utils]
            [mambobox.config])
  (:gen-class))

(defonce server (start-server :port 7777))


(defn current-username [req]
  (let [current-ident (friend/current-authentication req)
        username (get current-ident :username)]
    username))

(defn current-user-id [req]
  (let [current-ident (friend/current-authentication req)
        id (get current-ident :_id)]
    id))
                                     
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;            Secured Routes            ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes app-auth-routes
  ;;Music Page
  (GET "/music/" [q curpage tagfilter collection-filter :as req] (mc/music-search (current-user-id req)
                                                                                  q 
                                                                                  tagfilter 
                                                                                  collection-filter
                                                                                  curpage))

  (GET "/music/:id" [id :as req] (mc/music-id (current-user-id req)
                                              id))

  (GET "/music-surprise" [:as req] (mc/surprise-me  (current-user-id req)))

  (POST "/music/:id" [id newsongname newartist :as req] (mc/edit-music (current-user-id req)
                                                                       id 
                                                                       newsongname 
                                                                       newartist))

  (POST "/music/:musicid/tags/:tagname" [musicid tagname :as req] (mc/add-tag (current-username req)
                                                                              musicid
                                                                              tagname))

  (DELETE "/music/:musicid/tags/:tagname" [musicid tagname :as req] (mc/delete-tag (current-username req)
                                                                                   musicid
                                                                                   tagname))

  (POST "/music/:musicid/links/" [musicid newlink :as req] (mc/add-related-link (current-username req)
                                                                                musicid
                                                                                newlink))

  (DELETE "/music/:musicid/links/:link" [musicid link :as req] (mc/del-related-link (current-username req)
                                                                                    musicid 
                                                                                    link))

  (GET "/upload" [:as req] (mc/upload-page (current-username req)))

  (POST "/upload" [files :as req] (mc/upload-file (current-username req)
                                                  files))

  (POST "/current-user/favourites/:musicid" [musicid :as req] (uc/add-song-to-favourites musicid
                                                                                  (current-user-id req)
                                                                                  (current-username req)))

  (DELETE "/current-user/favourites/:musicid" [musicid :as req] (uc/del-song-from-favourites musicid
                                                                                             (current-user-id req)))

  ;; Home Page
  (GET "/" [:as req] (hc/home (current-username req)))

  ;; News
  (POST "/news/" [newtext :as req] (hc/add-new (current-username req) 
                                               newtext)) 

  ;; Logout
  (GET "/logout" req
    (friend/logout* (resp/redirect (str (:context req) "/"))))

  ;; Not Found
  (route/not-found "Not Found"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;        UNSecured Routes              ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes app-routes

  ;; Music Files
  (route/files "/files/" {:root mambobox.config/music-dir}) 
 
  ;; Login Page
  (GET "/login" [] (lv/login))

  (GET "/users/" [] (ucv/create-user-view))

  (POST "/users/" [firstname lastname username password password2 invitation] (uc/create-user firstname
                                                                                   lastname
                                                                                   username
                                                                                   password
                                                                                   password2
                                                                                   invitation))

  (GET "/favicon.ico" [] (resp/redirect "/"))
  (GET "/apple-touch-icon.png" [] (resp/redirect "/"))
  ;; Resources
  (route/resources "/"))

    
(def app
  (->
   (handler/site 
    (routes
     app-routes
     (friend/authenticate app-auth-routes
                          {:allow-anon? nil
                           :login-uri "/login"
                           :credential-fn (partial creds/bcrypt-credential-fn data/get-user-by-username)
                           :workflows [(workflows/interactive-form)]}))
    {:multipart {:store @utils/my-default-store}})
   (utils/wrap-mp3-files-contentype)
   (utils/wrap-my-exception-logger)))

(defn -main [& [port]]
  (let [port (Integer. (or port
                           (System/getenv "PORT")
                           80))]
     (jetty/run-jetty #'app {:port  port
                            :join? false})))
