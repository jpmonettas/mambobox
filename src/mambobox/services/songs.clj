(ns mambobox.services.songs
  (:require [mambobox.utils :as utils]
            [mambobox.data-access :as data]
            [taoensso.timbre :as l]
            [pantomime.mime :refer [mime-type-of]]
            [fuzzy-string.core :as fuzz-str]
            [clojure.string :refer [lower-case]]
            [clj-time.core :refer [weeks ago after?]]
            [clj-time.coerce :refer [from-date]]
            [slingshot.slingshot :refer [throw+ try+]]))

(defn get-all-tags []
  [{:name "chacha" :color "#ff0000"}
   {:name "mambo" :color "#9303a7"}
   {:name "latin-jazz" :color "#993366"}
   {:name "guaracha" :color "#64a8d1"}
   {:name "salsa dura" :color "#2219b2"}
   {:name "romantica" :color "#cb0077"}
   {:name "bolero" :color "#e5399e"}
   {:name "pachanga" :color "#999900"}
   {:name "boogaloo" :color "#d9534f"}
   {:name "son" :color "#ff7800"}
   {:name "montuno" :color "#ff9a40"}
   {:name "songo" :color "#ffa700"}
   {:name "danzon" :color "#ffbd40"}
   {:name "rumba" :color "#138900"}
   {:name "guaguanco" :color "#389e28"}
   {:name "yambu" :color "#1dd300"}
   {:name "columbia" :color "#52e93a"}
   {:name "afro" :color "#a64b00"}])

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

(defn get-song-by-id [db-cmp id]
  (data/get-song-by-id db-cmp id))

(defn get-all-songs
  ([db-cmp]
   (data/get-all-songs db-cmp))
  ([db-cmp ids]
   (data/get-all-songs-from-ids db-cmp ids)))

(defn search-music [q tag all-songs]
  (let [processed-q (when q (lower-case q))
        query-filtered-songs (if (not (empty? q)) 
                               (filter (fn [song]
                                         (accept-song-for-query? song processed-q))
                                       all-songs)
                               all-songs)
        tag-filtered-songs (if (not (empty? tag))
                             
                             (if (= tag "untagged") 
                               (filter (fn [song]
                                         (-> song :tags empty?))
                                       query-filtered-songs)
                               
                               (filter (fn [song]
                                         (song-contains-tag? song tag))
                                       query-filtered-songs))
                             
                             query-filtered-songs)]
    tag-filtered-songs))

(defn get-tags-freaquency-map [songs]
  (reduce (fn [map next] 
            (assoc map next (inc (get map next 0)))) 
          {}
          (reduce concat (map #(:tags %) songs))))



(defn get-cant-pages [col page-size]
  (let [col-size (count col)]
    (+ (quot col-size page-size) 
       (if (> (mod col-size page-size) 0) 1 0))))


(defn get-collection-page [cur-page page-size col]
  (let [col-size (count col)
        num-pages (get-cant-pages col page-size)
        first-song (inc (* (dec cur-page) page-size))
        last-song (dec (+ first-song page-size))]
    (utils/sub-list col first-song last-song)))
        

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

(defn track-song-access [db-cmp song-id]
  (data/track-song-access db-cmp song-id))

(defn save-song [db-cmp song-name song-artist file-name generated-filename username]
  (data/save-song db-cmp
                  song-name
                  song-artist
                  file-name
                  generated-filename
                  username))

(defn update-song [db-cmp song-id song-name artist]
  (data/update-song db-cmp song-id song-name artist))

(defn get-song-by-file-name [db-cmp file-name]
  (data/get-song-by-file-name db-cmp file-name))

(defn add-song-tag [db-cmp song-id tag-name]
  (let [tags-set (->> (get-all-tags) (map :name) (into #{}))]
    (if (tags-set tag-name)
      (data/add-song-tag db-cmp song-id tag-name)
      (throw+ {:type :invalid-input-data}))))

(defn del-song-tag [db-cmp song-id tag-name]
  (let [tags-set (->> (get-all-tags) (map :name) (into #{}))]
    (if (tags-set tag-name)
      (data/del-song-tag db-cmp song-id tag-name)
      (throw+ {:type :invalid-input-data}))))

(defn add-song-external-video-link [db-cmp song-id link]
  (data/add-song-external-video-link db-cmp song-id link))

(defn del-song-external-video-link [db-cmp song-id link]
  (data/del-song-external-video-link db-cmp song-id link))

(defn upload-file [db-cmp system-config username file-map]
  {:io? true}
  (l/debug "Uploading a file for user : " username)
  (l/debug "File : " file-map)
  (l/debug "Format : " (mime-type-of (:tempfile file-map)))
  (if (= (mime-type-of (:tempfile file-map)) "audio/mpeg")
    (try+
     (let [file-name (file-map :filename)
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
           existing-song-for-sum (get-song-by-file-name db-cmp generated-file-name)]
       (if existing-song-for-sum
         (do  
           (l/warn username "has uploaded a file:[" file-name "] of size [" size "] that is already on mambobox so skipping.")
           (throw+ {:type :upload-fail
                    :message (str "El archivo ya existe con nombre de tema : " (get existing-song-for-sum :name))
                    :filename file-name
                    :size size}))
         (do 
           (utils/save-file-to-disk file-map generated-file-name (:music-dir system-config))
           (let [created-song (save-song db-cmp
                                         song-name
                                         song-artist
                                         file-name
                                         generated-file-name username)] 
             (l/info username "uploaded a file:[" file-name "] of size [" size "]")
             (l/info "FS generated name : " generated-file-name)
             (when (not metadata-tags) (l/info "We couldn't find any file ID3 tag"))
             created-song))))
     (catch [:type :upload-fail] {:keys [message filename size]}       
       {:files [{:name filename
                 :size size
                 :error message}]}))

    ;;It's not an mp3 file
    (throw+ {:type ::format-not-supported :format (mime-type-of (:tempfile file-map))})))




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

