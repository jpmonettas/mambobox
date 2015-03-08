(ns mambobox.utils
  (:use [ring.middleware.params]
        [ring.middleware.multipart-params]
        [ring.adapter.jetty]
        [ring.util.response]
        [compojure.core]
        [taoensso.timbre :as log]
        [clojure.java.io :as io]
        [digest]
        [clj-time.format :as ctf]
        [clj-time.coerce :as ctc]
        [clj-stacktrace.repl])
  (:import [java.io File]
           [org.bson.types ObjectId]
           [java.util UUID]
           [java.lang Integer]
           [org.jaudiotagger.audio AudioFileIO]
           [org.jaudiotagger.tag FieldKey]))


(defn gen-uuid [file] (md5 file))

(defn get-name-from-email [email]
  (get (re-find #"(.*)@.*" email) 1))


(defn format-date [date]
  (let [formatter (ctf/formatter "dd/MM/yyyy hh:mm")]
    (ctf/unparse formatter (from-date date))))

(defn save-file-to-disk [file new-file-name dest-dir]
  (let [actual-file (file :tempfile)]
      (copy actual-file (File. (str dest-dir new-file-name)))))

(defn str-contains [str-1 str-2]
  (. str-1 contains str-2))

(defn sub-list [col from & to]
  (let [before-to (if to (take (first to) col) col)
        after-from (drop (dec from) before-to)]
    after-from))

(defn parse-int [str]
  (Integer/parseInt str))

(defn make-random-subset [col size]
  (sub-list (shuffle col) 0 size))

(defn wrap-my-exception-logger [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception ex
        (let [msg (str "DAMN IT!!!: " (pst-str ex))]
          (log/error msg)
          (throw ex))))))

(defn wrap-mp3-files-contentype [handler]
  (fn [request]
    (if (str-contains (:uri request) "/files/")
        (let [response (content-type (handler request) "audio/mpeg")]
          response)
    (handler request))))

        

(defn wrap-debug [handler]
  (fn [request]
      (let [response (handler request)]
        (log/info response)
        response)))

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

;; (with-auto-object-id [user-id]
;;     (mc/update "users" {:_id user-id} {$addToSet {:visited song-id}}))

;; Macroexpands to ->

;; (let [user-id (if (instance? java.lang.String user-id)
;;                 (ObjectId. user-id)
;;                 user-id)]
;;   (mc/update "users" {:_id user-id} {$addToSet {:visited song-id}}))

(defmacro with-auto-object-id [ids & forms]
  `(let 
       ~(into []
              (reduce 
               concat
               (map
                (fn [bind-name]
                  [bind-name `(if (vector? ~bind-name)
                                (if (instance? String (first ~bind-name))
                                  (map #(ObjectId. %) ~bind-name)
                                  ~bind-name)
                                (if (instance? String ~bind-name)
                                  (ObjectId. ~bind-name)
                                  ~bind-name))])
                ids)))
     ~@forms))



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
    (catch Exception e (log/error "Error while reading music file tag"))))




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



;; (dlet [a 5
;;        b (i)
;;        d (d* (+ 5 6))]
;;    (f1)
;;    (f2))

;; (let [a (j)
;;       b (i)
;;       d (let [XX w]
;;           (print XX)
;;           XX)]
;;   (f1)
;;   (f2))


(defn symbol-starts-with-*? [sym]
  (= (get (name sym) 0) \*))

(defn remove-mark-from-symbol [sym]
  (symbol (subs (name sym) 1)))

(defmacro dlet [bindings & body-forms]
  `(let 
       ~(let [bind-pairs (partition 2 bindings)]
          (into []
                (reduce 
                 concat
                 (map 
                  (fn [[bind-name bind-form]]
                    (if (symbol-starts-with-*? bind-name) 
                      [(remove-mark-from-symbol bind-name) `(let [res# ~bind-form]
                                                              (log/debug (quote ~bind-name)
                                                                         ":"
                                                                         (with-out-str (clojure.pprint/pprint res#)))
                                                              res#)]
                      [bind-name bind-form]))
                  bind-pairs))))
     ~@body-forms))
  


