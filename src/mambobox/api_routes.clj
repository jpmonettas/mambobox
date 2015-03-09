(ns mambobox.api-routes
  (:require [ring.util.http-response :refer :all]
            [mambobox.controllers.music :as mc]
            [mambobox.data-access :as data]
            [compojure.api.sweet :refer :all]
            [taoensso.timbre :as l]
            [schema.core :as s]))


(s/defschema Song {:id String
                   :artist String
                   :name String
                   :tags [String]
                   :visits Number
                   :uploader-username String})

(defn song->json-song [s]
  (-> s
     (assoc :id (-> s :_id str))
     (select-keys [:id :artist :name :tags :visits :uploader-username])))


(defapi api-routes
  (swagger-ui "/api/")
  (swagger-docs :title "Mambobox api")
  (swaggered
   "Songs" :description "Songs api"
   (context "/api/songs" []

            (GET* "/" [:as req]
                  :return [Song]
                  :query-params [{q :- String ""} {tag :- String ""}]
                  :summary "Search songs by query and tag"
                  (let [db-cmp (:db-cmp req)
                        all-songs (data/get-all-songs db-cmp)]
                    (l/debug "Searchig for " q " and " tag)
                    (ok (->> (mc/search-music q tag all-songs)
                           (map song->json-song))))))))
