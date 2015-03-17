(ns mambobox.api-routes
  (:require [ring.util.http-response :refer :all]
            [mambobox.services.songs :as ss]
            [mambobox.services.users :as us]
            [compojure.api.sweet :refer :all]
            [taoensso.timbre :as l]
            [schema.core :as s]))


(s/defschema Song {:id String
                   :artist String
                   :name String
                   (s/optional-key :tags) [String]
                   :visits Number
                   :uploader-username String})

(defn song->json-song [s]
  (l/debug s)
  (-> s
     (assoc :id (-> s :_id str))
     (select-keys [:id :artist :name :tags :visits :uploader-username])))

(defn user-for-token [db-cmp token]
  (case token
    "111" (us/get-user-by-username db-cmp "user")
    "666" (us/get-user-by-username db-cmp "admin")))

;; For understanding this, grab one of the GET*,POST* that contains :auth and
;; macroexpand them
(defmethod compojure.api.meta/restructure-param :auth
  [_ [current-user-binding roles] {:keys [parameters lets body middlewares] :as acc}]
  ""
  (-> acc
     (update-in [:lets] (fn [lets] (conj lets current-user-binding
                                        '(->> +compojure-api-request+
                                            :params
                                            :api_key
                                            (user-for-token (-> +compojure-api-request+ :db-cmp))))))
     
     (assoc :body `((if (and ~current-user-binding
                           (~roles (:role ~current-user-binding)))
                      (try (do ~@body) (catch Exception ex# (l/error ex#)))
                      (ring.util.http-response/forbidden "Auth required"))))))

(defapi api-routes
  (swagger-ui "/api/")
  (swagger-docs :title "Mambobox api")
  (swaggered
   "Songs" :description "Songs api"
   (context "/api/songs" []

            (GET* "/" [:as req]
                  :return [Song]
                  :auth [current-user #{:normal-user :admin-user}]
                  :query-params [{q :- String ""} {tag :- String ""}]
                  :summary "Search songs by query and tag"
                  (let [db-cmp (:db-cmp req)
                        all-songs (ss/get-all-songs db-cmp)]
                    (l/debug "Searchig for " q " and " tag)
                    (ok (->> (ss/search-music q tag all-songs) 
                           (map song->json-song)))))


            (POST* "/" [file :as req]
                   :return Song
                   :auth [current-user #{:normal-user :admin-user}]
                   :summary "Upload an mp3 or wma song as multipart-form data under file key"
                   (let [{:keys [db-cmp system-config]} req]
                     (ok (song->json-song (ss/upload-file db-cmp
                                                          system-config
                                                          (:username current-user)
                                                          file)))))

            (PUT* "/:song-id/listened" [file :as req]
                  :auth [current-user #{:normal-user :admin-user}]
                  :path-params [song-id :- String]
                  :summary "Tracks a song as listened by the user"
                  (let [{:keys [db-cmp system-config]} req]
                    (ss/track-song-access db-cmp song-id)
                    (ok)))

            (PUT* "/:song-id" [file :as req]
                  :return Song
                  :auth [current-user #{:admin-user}]
                  :path-params [song-id :- String]
                  :query-params [{new-song-name :- String ""}
                                 {new-song-artist :- String ""}]
                  :summary "Updated song name and/or artist"
                  (let [{:keys [db-cmp system-config]} req]
                    (-> (ss/update-song db-cmp song-id new-song-name new-song-artist)
                       song->json-song
                       ok)))))
  
  (swaggered
   "Users" :description "Users api"
   (context "/api/users" []

            (POST* "/my-user/favourites/:song-id" [file :as req]
                   :summary "Add song to users favourites"
                   :auth [current-user #{:normal-user :admin-user}]
                   :path-params [song-id :- String]
                   (let [{:keys [db-cmp system-config]} req]
                     (us/add-song-to-favourites db-cmp song-id (:_id current-user))
                     (ok)))

            (GET* "/my-user/favourites" [file :as req]
                  :return [Song]
                  :summary "Retrieve user favourites songs"
                  :auth [current-user #{:normal-user :admin-user}]
                  (let [{:keys [db-cmp system-config]} req]
                    (->> (us/get-user-favourites-songs db-cmp (:_id current-user))
                       (map song->json-song)
                       (ok))))

            (DELETE* "/my-user/favourites/:song-id" [file :as req]
                     :summary "Delete song from users favourites"
                     :auth [current-user #{:normal-user :admin-user}]
                     :path-params [song-id :- String]
                     (let [{:keys [db-cmp system-config]} req]
                       (us/del-song-from-favourites db-cmp song-id (:_id current-user))
                       (ok))))))
