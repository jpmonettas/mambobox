(ns mambobox.controllers.music
  (:use [mambobox.views.music-search :only [music-search-view]]
        [mambobox.views.music-detail :only [music-detail-view]]
        [mambobox.views.music-upload :only [music-upload-view]]
        [ring.util.response]
        [mambobox.utils :only [defnlog dlet]]
        [slingshot.slingshot :only [throw+ try+]]
        [clj-time.core :only  [weeks ago after?]]
        [clj-time.coerce :only  [from-date]]
        [clojure.string :only [lower-case]])
  (:require [mambobox.utils :as utils]
            [mambobox.data-access :as data]
            [taoensso.timbre :as log]
            [fuzzy-string.core :as fuzz-str]
            [clojure.data.json :as json]
            [clojure.set :as set]))


(defn get-tags-freaquency-map [songs]
  (reduce (fn [map next] 
            (assoc map next (inc (get map next 0)))) 
          {}
          (reduce concat (map #(:tags %) songs))))


(defn accept-song-for-query? [song qstring]
  (let [song-name (lower-case (get song :name))
        song-artist (lower-case (get song :artist))]                              
    (or (> (fuzz-str/dice song-name qstring) 0.5) 
        (> (fuzz-str/dice song-artist qstring) 0.5)
        (and (> (count qstring) 3)
             (or
              (utils/str-contains song-name qstring)
              (utils/str-contains song-artist qstring)))))) 
                          
(defn song-contains-tag? [song tag]
  (let [tags (get song :tags)]
    (some #{tag} tags)))

(defn get-cant-pages [col page-size]
  (let [col-size (count col)]
    (+ (quot col-size page-size) 
       (if (> (mod col-size page-size) 0) 1 0))))

(defn get-collection-page [col cur-page page-size]
  (let [col-size (count col)
        num-pages (get-cant-pages col page-size)
        first-song (inc (* (dec cur-page) page-size))
        last-song (dec (+ first-song page-size))]
    (utils/sub-list col first-song last-song)))
        

(defn search-music [q tag all-songs]
  (dlet [processed-q (when q (lower-case q))
        query-filtered-songs (if (not (empty? q)) 
                               (filter (fn [song]
                                         (accept-song-for-query? song processed-q))
                                       all-songs)
                               all-songs)
        tag-filtered-songs (if (not (empty? tag)) 
                             (filter (fn [song]
                                       (song-contains-tag? song tag))
                                     query-filtered-songs)
                             query-filtered-songs)]
        tag-filtered-songs))

(defn get-all-favourites [users]
  (into #{} (reduce concat (map #(:favourites %) users))))

(defn get-ids-from-songs [songs]
  (when songs
    (into #{} (map #(.toString (:_id %)) songs))))

(defn zero-score-songs [songs]
  (map #(assoc % :score 0) songs))

(defn favourites-score-songs [songs favourites-ids points]
  (map (fn [song]
         (let [song-id (:_id song)]
           (utils/with-auto-object-id [song-id favourites-ids]
             (if (some #{song-id} favourites-ids)
               (assoc song :score (+ (:score song) points))
               song))))
           songs))

(defn visits-score-songs [songs]
  (let [most-visited-value (reduce max 0 (map #(:visits %) songs))]
    (map (fn [song]
           (let [visits-percentage-points (/ (* (:visits song) 100) most-visited-value)]
             (assoc song :score (+ (:score song) visits-percentage-points))))
         songs)))

(defn zero-score-user-fav-songs [songs user-favs-songs-ids]
  (map (fn [song]
         (let [song-id (:_id song)]
           (utils/with-auto-object-id [song-id user-favs-songs-ids]
             (if (some #{song-id} user-favs-songs-ids)
               (assoc song :score 0)
               song))))
           songs))

(defn newers-score-songs [songs newer-bottom-limit]
  (map (fn [song]
         (let [song-date-created (from-date (:date-created song))]
           (if (and newer-bottom-limit (after? song-date-created newer-bottom-limit))
               (assoc song :score (+ (:score song) 50))
             song)))
       songs))
           

(defn make-scored-songs-col [all-songs user-favourites-ids all-favourites-ids newer-bottom-limit]
    (->
     all-songs
     (zero-score-songs)
     (favourites-score-songs all-favourites-ids 100)
     (visits-score-songs)
     (newers-score-songs newer-bottom-limit)
     (zero-score-user-fav-songs user-favourites-ids)))


(defn sort-songs-by-score [songs]
  (sort-by :score > songs))

(defn make-top-suggested-songs-col [scored-songs suggeste-scored-percentage]
  (let [top-suggested-size (int (/ (* (count scored-songs) suggeste-scored-percentage) 100))]
    (-> 
     scored-songs 
     (sort-songs-by-score)
     (utils/sub-list 0 top-suggested-size))))
   
(defn get-suggested-songs-for-user [db-cmp user-id suggeste-scored-percentage suggesteds-size]
  {:io? true}
  (let [all-songs (data/get-all-songs db-cmp)
        user (data/get-user-by-id db-cmp user-id)
        user-favs-ids (:favourites user)
        all-users (data/get-all-users db-cmp)
        all-favourites-ids (get-all-favourites all-users)
        newer-bottom-limit (-> 3 weeks ago)]
    (->
     (make-scored-songs-col all-songs user-favs-ids all-favourites-ids newer-bottom-limit)
     (make-top-suggested-songs-col suggeste-scored-percentage)
     (utils/make-random-subset suggesteds-size))))
        
  

;; (defn pprint-songs-scorecard [db-cmp]
;;   (let [all-songs (data/get-all-songs db-cmp)
;;         all-users (data/get-all-users db-cmp)
;;         all-favourites-ids (get-all-favourites all-users)
;;         scored-songs (make-scored-songs-col all-songs nil all-favourites-ids (-> 3 weeks ago))
;;         sorted-scored-songs (sort-songs-by-score scored-songs)]
;;     (doseq [song sorted-scored-songs]
;;         (let [name (:name song)
;;                 artist (:artist song)
;;                 date-created (:date-created song)
;;                 visits (:visits song)
;;                 score (:score song)]
;;             (log/debug (int score) name "(" artist ") v:" visits "uploaded:" date-created)))
;;         (log/debug "Suggesting the first :" (int (/ (* (count scored-songs) config/suggeste-scored-percentage) 100)))))

        
(defn surprise-me [db-cmp system-config user-id]
  {:io? true}
  (let [user (data/get-user-by-id db-cmp user-id)
        suggeste-scored-percentage (:suggested-score-percentage system-config)
        suggesteds-size (:suggesteds-size system-config)
        surprise-me-vector (into [] (get-suggested-songs-for-user user-id suggeste-scored-percentage suggesteds-size))
        random-song-index (int (rand (count surprise-me-vector)))
        song (get surprise-me-vector random-song-index)]
    (log/info "Song" (:name song) "choosed for" (:usename user))
    (redirect (str "/music/" (:_id song)))))
                

(defn music-search [db-cmp system-config user-id q tag collection-filter cur-page]
  {:io? true}
  (let [user (data/get-user-by-id db-cmp user-id)
        username (:username user)
        user-favourite-song-ids (:favourites user)
        collection-filter (if collection-filter collection-filter "all")
        cur-page (if cur-page (utils/parse-int cur-page) 1)
        base-collection (cond (= collection-filter "all") (data/get-all-songs db-cmp)
                              (= collection-filter "favourites") (when-not (empty? user-favourite-song-ids)
                                                                   (data/get-all-songs-from-ids db-cmp user-favourite-song-ids)))
        search-result (search-music q tag base-collection)
        tags-freaquency-map (get-tags-freaquency-map search-result)
        num-pages (get-cant-pages search-result (:result-page-size system-config))
        cur-page-songs (get-collection-page search-result cur-page (:result-page-size system-config))]
    (log/info username "searching for [" q "] with tag [" tag "] and page" cur-page "retrieved" (count search-result) "songs")
    (music-search-view username
                       cur-page-songs
                       q
                       tag
                       collection-filter
                       cur-page
                       num-pages
                       tags-freaquency-map
                       (= collection-filter "favourites"))))

(defn is-song-user-favourite? [song-id user]
  (let [favourites (:favourites user)]
        (some #{(.toString song-id)} favourites)))


(defn music-id [db-cmp user-id song-id]
  {:io? true}
  (let [song (data/get-song-by-id db-cmp song-id)
        user (data/get-user-by-id db-cmp user-id)
        username (:username user)
        selected-suggesteds-songs (get-suggested-songs-for-user user-id)]
    (data/track-song-access db-cmp song-id)
    (data/add-song-to-visited db-cmp user-id song-id)
    (log/info username "seeing" (:name song) "[" song-id "]")
    (music-detail-view username song (is-song-user-favourite? song-id user) selected-suggesteds-songs)))

(defn edit-music [db-cmp user-id song-id song-name artist]
  {:io? true} 
  (let [user (data/get-user-by-id db-cmp user-id)
        username (:username user)
        song (data/update-song db-cmp song-id song-name artist)
        selected-suggesteds-songs (get-suggested-songs-for-user user-id)]
    (log/info username "editing song [" song-id "] with new name : [" song-name "] and new artist : [" artist "]")
    (music-detail-view username song (is-song-user-favourite? song-id user) selected-suggesteds-songs)))

(defn upload-page [username]
  (music-upload-view username))

(defn upload-file [db-cmp system-config username file]
  {:io? true}
  (try+
   (let [file-map (first file)
         file-name (file-map :filename)
         temp-file (file-map :tempfile)
         size (file-map :size)
         generated-file-name (utils/gen-uuid temp-file) ;; md5sum
         metadata (utils/get-metadata temp-file)
         metadata-tags (when metadata
                         (metadata :tags))
         title-tag (when metadata-tags
                     (first (metadata-tags :title)))
         artist-tag (when metadata-tags
                      (first (metadata-tags :artist)))
         song-name (if (not (empty? title-tag)) title-tag file-name)
         song-artist (if (not (empty? artist-tag)) artist-tag "Desconocido")
         existing-song-for-sum (data/get-song-by-file-name db-cmp generated-file-name)]
     (if existing-song-for-sum
       (do  
         (log/warn username "has uploaded a file:[" file-name "] of size [" size "] that is already on mambobox so skipping.")
         (throw+ {:type :upload-fail
                  :message (str "El archivo ya existe con nombre de tema : " (get existing-song-for-sum :name))
                  :filename file-name
                  :size size}))
       (do 
         (utils/save-file-to-disk file-map generated-file-name (:music-dir system-config))
         (let [created-song (data/save-song db-cmp
                                            song-name
                                            song-artist
                                            file-name
                                            generated-file-name username)] 
           (log/info username "uploaded a file:[" file-name "] of size [" size "]")
           (log/info "FS generated name : " generated-file-name)
           (when (not metadata-tags) (log/info "We couldn't find any file ID3 tag"))
           (json/write-str {:files [{:name file-name
                                     :size size
                                     :url (str "/music/" (get created-song :_id))}]})))))
   (catch [:type :upload-fail] {:keys [message filename size]}       
     (json/write-str {:files [{:name filename
                               :size size
                               :error message}]}))))



(defn add-tag [db-cmp username song-id tag-name]
  {:io? true}
  (data/add-song-tag db-cmp song-id tag-name)
  (log/info username "has tagged" song-id "with" tag-name)
  {:status 204})

(defn delete-tag [db-cmp username song-id tag-name]
  {:io? true}
  (data/del-song-tag db-cmp song-id tag-name)
  (log/info username "has deleted from" song-id "tag" tag-name)
  {:status 204})

(defn is-youtube-mobile-link? [link]
   (re-matches #".*youtu.be/[a-zA-Z0-9\-_]+" link))

(defn is-youtube-desktop-link? [link]
   (re-matches #".*youtube.com/watch\?.*v=[a-zA-Z0-9\-_]+.*" link))

(defn is-youtube-link? [link]
  (or (is-youtube-desktop-link? link) (is-youtube-mobile-link? link)))

(defn get-youtube-video-id [link]
  (cond 
   (is-youtube-mobile-link? link) (second (re-matches #".*youtu.be/([a-zA-Z0-9\-_]+)" link))
   (is-youtube-desktop-link? link) (second (re-matches #".*youtube.com/watch\?.*v=([a-zA-Z0-9\-_]+).*" link))))

(defn gen-youtube-embeded-link [video-id]
  (str "http://www.youtube.com/embed/" video-id))

(defn add-related-link [db-cmp username song-id link]
  {:io? true}
  (if (is-youtube-link? link)
    (let [video-id (get-youtube-video-id link)
          youtube-embeded-link (gen-youtube-embeded-link video-id)]
      (data/add-song-external-video-link db-cmp song-id youtube-embeded-link)
      (log/info username "has added a related youtube video(" link ") for song" song-id)
      (redirect (str "/music/" song-id)))
    {:status 400}))

(defn del-related-link [db-cmp username song-id link]
  {:io? true}
  (data/del-song-external-video-link db-cmp song-id link)
  (log/info username "has deleted" link "from the song" song-id)
  {:status 204})
