(ns mambobox.data-access
  (:require [monger.core :as mg]
            [monger.collection :as mc]))

;; localhost, default port
(mg/connect!)
(mg/set-db! (mg/get-db "test"))

(defn get-all-tests []
  (mc/find-maps "test"))