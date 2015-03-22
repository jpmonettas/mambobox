(ns mambobox.api-routes
  (:require [ring.util.http-response :refer :all]
            [mambobox.services.songs :as ss]
            [mambobox.services.users :as us]
            [compojure.api.sweet :refer :all]
            [taoensso.timbre :as l]
            [schema.core :as s]
            [slingshot.slingshot :refer [throw+ try+]]))


(s/defschema Song {:id String
                   :artist String
                   :name String
                   (s/optional-key :tags) [String]
                   :visits Number
                   :uploader-username String
                   :music-file-url String})

(s/defschema Tag {:name String
                  :color String})

(s/defschema User {:id String
                   :first-name String
                   :last-name String
                   :username String
                   :role (s/enum :admin-user :normal-user)})

(defn user->json-user [u]
  (-> u
     (assoc :id (-> u :_id str))
     (select-keys [:id :first-name :last-name :username :role])))

(defn song->json-song [s]
  (-> s
     (assoc :id (-> s :_id str))
     (assoc :music-file-url (str "/files/" (:generated-file-name s)))
     (select-keys [:id :artist :name :tags :visits :uploader-username :music-file-url])))

(defn user-for-token [db-cmp token]
  (case token
    "111" (us/get-user-by-username db-cmp "user")
    "666" (us/get-user-by-username db-cmp "admin")
    nil))

;; For understanding this, grab one of the GET*,POST* that contains :auth and
;; macroexpand them
(defmethod compojure.api.meta/restructure-param :auth
  [_ [current-user-binding roles] {:keys [lets body] :as acc}]
  ""
  (-> acc
     (update-in [:lets] (fn [lets] (conj lets current-user-binding
                                        '(->> +compojure-api-request+
                                            :params
                                            :api_key
                                            (user-for-token (-> +compojure-api-request+ :db-cmp))))))
     
     (assoc :body `((if (and ~current-user-binding
                           (~roles (:role ~current-user-binding)))
                      (do ~@body)
                      (ring.util.http-response/forbidden "Auth required"))))))


(defn mambobox-api-exception-handler
  "An exception handler that logs the exception and returns an internal error (HTTP 500)"
  [^Exception e]
  (if (= (type e) clojure.lang.ExceptionInfo)
    (do
      (l/error (:object (.getData e)))
      (internal-server-error {:slingshot-object (:object (.getData e))}))
    (do
      (l/error e)
      (internal-server-error {:type  "unhandled-exception"
                              :class (.getName (.getClass e))
                              :stacktrace (map str (.getStackTrace e))}))))

(defapi api-routes
  {:exceptions {:exception-handler mambobox-api-exception-handler}}
  (swagger-ui "/api/")
  (swagger-docs :title "Mambobox api")
  (swaggered
   "Songs" :description "Songs api"
   (context "/api/songs" []

            (GET* "/" [:as req]
                  :return [Song]
                  :auth [current-user #{:normal-user :admin-user}]
                  :query-params [{q :- String ""} {tag :- String ""} {page :- Long 1}]
                  :summary "Search songs by query and tag. If tag is untagged, returns untagged songs"
                  (let [db-cmp (:db-cmp req)
                        system-config (:system-config req)
                        all-songs (ss/get-all-songs db-cmp)]
                    (l/debug "Searchig for " q " and " tag)
                    (ok (->> (ss/search-music q tag all-songs)
                           (ss/get-collection-page page (:result-page-size system-config))
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

            (PUT* "/:song-id/listened" [:as req]
                  :auth [current-user #{:normal-user :admin-user}]
                  :path-params [song-id :- String]
                  :summary "Tracks a song as listened by the user"
                  (let [{:keys [db-cmp system-config]} req]
                    (ss/track-song-access db-cmp song-id)
                    (ok)))

            (PUT* "/:song-id" [:as req]
                  :return Song
                  :auth [current-user #{:admin-user}]
                  :path-params [song-id :- String]
                  :query-params [{new-song-name :- String ""}
                                 {new-song-artist :- String ""}]
                  :summary "Updated song name and/or artist"
                  (let [{:keys [db-cmp system-config]} req]
                    (-> (ss/update-song db-cmp song-id new-song-name new-song-artist)
                       song->json-song
                       ok)))

            (POST* "/:song-id/tags/:tag-id" [:as req]
                  :return Song
                  :auth [current-user #{:admin-user}]
                  :path-params [song-id :- String
                                tag-id :- String]
                  :summary "Add a tag to a song"
                  (let [{:keys [db-cmp system-config]} req]
                    (try+
                     (-> (ss/add-song-tag db-cmp song-id tag-id)
                        song->json-song
                        ok)
                     (catch [:type :invalid-input-data] ex-map
                       (bad-request "Invalid input data. Something on the parameters is wrong.")))))

            (DELETE* "/:song-id/tags/:tag-id" [:as req]
                  :return Song
                  :auth [current-user #{:admin-user}]
                  :path-params [song-id :- String
                                tag-id :- String]
                  :summary "Deletes a tag from a song"
                  (let [{:keys [db-cmp system-config]} req]
                    (try+
                     (-> (ss/del-song-tag db-cmp song-id tag-id)
                        song->json-song
                        ok)
                     (catch [:type :invalid-input-data] ex-map
                       (bad-request "Invalid input data. Something on the parameters is wrong.")))))

            (GET* "/tags" [:as req]
                  :return [Tag]
                  :auth [current-user #{:admin-user :normal-user}]
                  :summary "Retrieves all tags you can add to a song"
                  (let [{:keys [db-cmp system-config]} req]
                    (ok (ss/get-all-tags))))))

  
  (swaggered
   "Users" :description "Users api"
   (context "/api/users" []

            (POST* "/my-user/favourites/:song-id" [:as req]
                   :summary "Add song to users favourites"
                   :auth [current-user #{:normal-user :admin-user}]
                   :path-params [song-id :- String]
                   (let [{:keys [db-cmp system-config]} req]
                     (us/add-song-to-favourites db-cmp song-id (:_id current-user))
                     (ok)))

            (GET* "/my-user/favourites" [:as req]
                  :return [Song]
                  :summary "Retrieve user favourites songs"
                  :auth [current-user #{:normal-user :admin-user}]
                  (let [{:keys [db-cmp system-config]} req]
                    (->> (us/get-user-favourites-songs db-cmp (:_id current-user))
                       (map song->json-song)
                       (ok))))

            (PUT* "/:user-id/promote" [:as req]
                  :return User
                  :summary "Promote a user to admin user"
                  :path-params [user-id :- String]
                  :auth [current-user #{:admin-user}]
                  (let [{:keys [db-cmp system-config]} req]
                    (-> (us/promote-user db-cmp user-id)
                       user->json-user
                       (ok))))

            (PUT* "/:user-id/demote" [:as req]
                  :return User
                  :summary "Demote a user to normal user"
                  :path-params [user-id :- String]
                  :auth [current-user #{:admin-user}]
                  (let [{:keys [db-cmp system-config]} req]
                    (-> (us/demote-user db-cmp user-id)
                       user->json-user
                       (ok))))

            (DELETE* "/my-user/favourites/:song-id" [:as req]
                     :summary "Delete song from users favourites"
                     :auth [current-user #{:normal-user :admin-user}]
                     :path-params [song-id :- String]
                     (let [{:keys [db-cmp system-config]} req]
                       (us/del-song-from-favourites db-cmp song-id (:_id current-user))
                       (ok))))))
