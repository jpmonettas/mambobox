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
  (when (= token "123")
    (us/get-user-by-username db-cmp "jpmonettas@gmail.com")))

;; For understanding this, grab one of the GET*,POST* that contains :auth and
;; macroexpand them
(defmethod compojure.api.meta/restructure-param :auth
  [_ current-user-binding {:keys [parameters lets body middlewares] :as acc}]
  ""
  (-> acc
     (update-in [:lets] (fn [lets] (conj lets current-user-binding
                                        '(->> +compojure-api-request+
                                            :params
                                            :api_key
                                            (user-for-token (-> +compojure-api-request+ :db-cmp))))))
     
     (assoc :body `((if ~current-user-binding
                      (do ~@body)
                      (ring.util.http-response/forbidden "Auth required"))))))

(defapi api-routes
  (swagger-ui "/api/")
  (swagger-docs :title "Mambobox api")
  (swaggered
   "Songs" :description "Songs api"
   (context "/api/songs" []

            (GET* "/" [:as req]
                  :return [Song]
                  :auth current-user
                  :query-params [{q :- String ""} {tag :- String ""}]
                  :summary "Search songs by query and tag"
                  (let [db-cmp (:db-cmp req)
                        all-songs (ss/get-all-songs db-cmp)]
                    (l/debug "Searchig for " q " and " tag)
                    (ok (->> (try (ss/search-music q tag all-songs) (catch Exception e (l/error e)))
                           (map song->json-song)))))


            (POST* "/" [file :as req]
                   :return Song
                   :auth current-user
                   :summary "Upload an mp3 or wma song as multipart-form data under file key"
                   (let [{:keys [db-cmp system-config]} req]
                     (ok (song->json-song (ss/upload-file db-cmp
                                                          system-config
                                                          (:username current-user)
                                                          file)))))))
  (swaggered
   "Users" :description "Users api"
   (context "/api/users" []

            (POST* "/my-user/favourites/:song-id" [file :as req]
                   :summary "Add song to users favourites"
                   :auth current-user
                   :path-params [song-id :- String]
                   (let [{:keys [db-cmp system-config]} req]
                     (l/debug "Adding song " song-id " to user : " (:_id current-user))
                     (us/add-song-to-favourites db-cmp song-id (:_id current-user))
                     (ok))))))
