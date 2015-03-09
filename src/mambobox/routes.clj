(ns mambobox.routes
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
            [mambobox.utils :as utils])
  (:gen-class))


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
  (GET "/music/" [q curpage tagfilter collection-filter :as req] (let [{:keys [db-cmp system-config]} req]
                                                                   (mc/music-search db-cmp
                                                                                    system-config
                                                                                    (current-user-id req)
                                                                                    q 
                                                                                    tagfilter 
                                                                                    collection-filter
                                                                                    curpage)))

  (GET "/music/:id" [id :as req] (let [{:keys [db-cmp]} req]
                                   (mc/music-id db-cmp
                                                (current-user-id req)
                                                id)))

  (GET "/music-surprise" [:as req] (let [{:keys [db-cmp system-config]} req]
                                     (mc/surprise-me  db-cmp
                                                      system-config
                                                      (current-user-id req))))

  (POST "/music/:id" [id newsongname newartist :as req] (let [{:keys [db-cmp]} req]
                                                          (mc/edit-music db-cmp
                                                                         (current-user-id req)
                                                                         id 
                                                                         newsongname 
                                                                         newartist)))

  (POST "/music/:musicid/tags/:tagname" [musicid tagname :as req] (let [{:keys [db-cmp]} req]
                                                                    (mc/add-tag db-cmp
                                                                                (current-username req)
                                                                                musicid
                                                                                tagname)))

  (DELETE "/music/:musicid/tags/:tagname" [musicid tagname :as req] (let [{:keys [db-cmp]} req]
                                                                      (mc/delete-tag db-cmp
                                                                                     (current-username req)
                                                                                     musicid
                                                                                     tagname)))

  (POST "/music/:musicid/links/" [musicid newlink :as req] (let [{:keys [db-cmp]} req]
                                                             (mc/add-related-link db-cmp
                                                                                  (current-username req)
                                                                                  musicid
                                                                                  newlink)))

  (DELETE "/music/:musicid/links/:link" [musicid link :as req] (let [{:keys [db-cmp]} req]
                                                                 (mc/del-related-link db-cmp
                                                                                      (current-username req)
                                                                                      musicid 
                                                                                      link)))

  (GET "/upload" [:as req] (let [{:keys [db-cmp]} req]
                             (mc/upload-page db-cmp
                                             (current-username req))))

  (POST "/upload" [files :as req] (let [{:keys [db-cmp system-config]} req]
                                    (mc/upload-file db-cmp
                                                    system-config
                                                    (current-username req)
                                                    files)))

  (POST "/current-user/favourites/:musicid" [musicid :as req] (let [{:keys [db-cmp]} req]
                                                                (uc/add-song-to-favourites db-cmp
                                                                                           musicid
                                                                                           (current-user-id req)
                                                                                           (current-username req))))

  (DELETE "/current-user/favourites/:musicid" [musicid :as req] (let [{:keys [db-cmp]} req]
                                                                  (uc/del-song-from-favourites db-cmp
                                                                                               musicid
                                                                                               (current-user-id req))))

  ;; Home Page
  (GET "/" [:as req] (let [{:keys [db-cmp]} req]
                       (hc/home db-cmp
                                (current-username req))))

  ;; News
  (POST "/news/" [newtext :as req] (let [{:keys [db-cmp]} req]
                                     (hc/add-new db-cmp
                                                 (current-username req) 
                                                 newtext))) 

  ;; Logout
  (GET "/logout" req
    (friend/logout* (resp/redirect (str (:context req) "/"))))

  ;; Not Found
  (route/not-found "Not Found"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;        UNSecured Routes              ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes app-routes

  ;; Login Page
  (GET "/login" [] (lv/login))

  (GET "/users/" [] (ucv/create-user-view))

  (POST "/users/" [firstname lastname username password password2 invitation :as req] (let [{:keys [db-cmp]} req]
                                                                                        (uc/create-user db-cmp
                                                                                                        firstname
                                                                                                        lastname
                                                                                                        username
                                                                                                        password
                                                                                                        password2
                                                                                                        invitation)))

  (GET "/favicon.ico" [] (resp/redirect "/"))
  (GET "/apple-touch-icon.png" [] (resp/redirect "/"))
  ;; Resources
  (route/resources "/"))




