(ns mambobox.utils
  (:use [ring.middleware.params]
        [ring.middleware.multipart-params]
        [ring.adapter.jetty]
        [compojure.core]
        [clojure.java.io])
  (:import [java.io File]
           [java.util UUID]
           [java.lang Integer]))

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
