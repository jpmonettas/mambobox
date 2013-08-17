(ns mambobox.utils
  (:use [ring.middleware.params]
        [ring.middleware.multipart-params]
        [ring.adapter.jetty]
        [compojure.core]
        [clojure.tools.logging :as log]
        [clojure.java.io :as io])
  (:import [java.io File]
           [java.util UUID]
           [java.lang Integer]
           [org.jaudiotagger.audio AudioFileIO]
           [org.jaudiotagger.tag FieldKey]))

(defn gen-uuid [] (str (UUID/randomUUID)))

(defn save-file-to-disk [file new-file-name dest-dir]
  (let [size (file :size)
        actual-file (file :tempfile)]
    (do
      (copy actual-file (File. (str dest-dir new-file-name))))))

(defn str-contains [str-1 str-2]
  (. str-1 contains str-2))

(defn sub-list [col from & to]
  (let [before-to (if to (take (first to) col) col)
        after-from (drop (dec from) before-to)]
    after-from))

(defn parse-int [str]
  (Integer/parseInt str))

;; (defnlog test [a b] 
;;   (+ a b))

;; Macroexpands to : 

;; (defn test [a b] 
;;   (debug "p" 0 " " a)
;;   (debug "p" 1 " " b)
;;   (let [ret (+ a b)]
;;     (debug "Returned: " ret)
;;     ret))

(defmacro defnlog [fn-name params & body-forms]
  `(defn ~fn-name ~params
     ~@(map-indexed (fn [i p]
                      `(log/debug "p" ~i " " ~p))
                    params)
     (let [ret# ~@body-forms]
       (log/debug "Returned: " ret#)
       ret#)))




;; This if for the musci ID3 tag library
(defn tags [file]
  (let [fields (apply conj {} (map (fn [n] [(keyword (. (. n toString) toLowerCase)) n]) (. FieldKey values)))
        tag (. file (getTag))]
    (apply conj {}
           (filter (fn [[name val]] (and val (not (empty? val))))
                   (map (fn [[name val]]
                          [name (seq (map #(. % getContent) (. tag (getFields val))))])
                        fields)))))
 
(defn audioheader [file]
  (bean (. file (getAudioHeader))))

(defn get-metadata [tfile]
  (try
    (let [file (AudioFileIO/read tfile)]
      {:tags (tags file)
     :audioheader (audioheader file)})
    (catch Exception e (log/error "Error while reading musci file tag"))))




;; TODO Refactor this, move somewhere else
;; This is a compy from the original ring/middleware/multipart_params/temp_file.clj
;; addapted to save the temp file with a different extension

(defmacro ^{:private true} do-every [delay & body]
  `(future
     (while true
       (Thread/sleep (* ~delay 1000))
       (try ~@body
            (catch Exception ex#)))))

(defn- expired? [^File file expiry-time]
  (< (.lastModified file)
     (- (System/currentTimeMillis)
        (* expiry-time 1000))))

(defn- remove-old-files [file-set expiry-time]
  (doseq [^File file @file-set]
    (when (expired? file expiry-time)
      (.delete file)
      (swap! file-set disj file))))

(defn- ^File make-temp-file [file-set]
  (let [temp-file (File/createTempFile "ring-multipart-" ".mp3")]
    (swap! file-set conj temp-file)
    (.deleteOnExit temp-file)
    temp-file))

(defn my-temp-file-store
  ([] (my-temp-file-store {:expires-in 3600}))
  ([{:keys [expires-in]}]
     (fn [item]
       (let [file-set  (atom #{})
             temp-file (make-temp-file file-set)]
         (io/copy (:stream item) temp-file)
         (when expires-in
           (do-every expires-in
             (remove-old-files file-set expires-in)))
         (-> (select-keys item [:filename :content-type])
             (assoc :tempfile temp-file
                    :size (.length temp-file)))))))

(def my-default-store
  (delay (my-temp-file-store)))

