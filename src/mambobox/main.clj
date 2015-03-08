(ns mambobox.main
  (:require [mambobox.components.web-server :refer :all]
            [mambobox.components.timbre-logger :refer :all]
            [mambobox.components.db :refer :all]
            [com.stuartsierra.component :as component]
            [clojure.tools.nrepl.server :as nrepl]
            [clojure.edn :as edn]
            [cider.nrepl :refer [cider-nrepl-handler]])
  (:gen-class))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; System definition
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def system-config nil)

(def nrepl-server nil)

(def system nil)


;; Define system 
(defn create-system []
  (component/system-map
   :timbre-logger-cmp (create-timbre-logger-component system-config)
   :db-cmp (create-db-component system-config)
   :web-server-cmp (component/using
                    (create-web-server-component system-config)
                    [:timbre-logger-cmp :db-cmp])))


(defn start-system []
  (alter-var-root #'system (fn [s] (component/start (create-system)))))

(defn stop-system []
  (alter-var-root #'system
                  (fn [s] (when s (component/stop s)))))

(defn start-nrepl-server []
  (alter-var-root #'nrepl-server
                  (fn [s] (nrepl/start-server :handler cider-nrepl-handler :port (:nrepl-port system-config)))))

(defn load-config [file]
  (alter-var-root #'system-config (fn [v f] (edn/read-string (slurp f))) file))

(defn restart []
  (let [config-file "./resources/configs/dev.clj"]
    (when system (stop-system))
    (load-config config-file)
    (start-system)))

(defn -main
  [& args]
  
  (let [config-file (first args)]
    (load-config config-file)
    (start-nrepl-server)
    (start-system)))


(defn d [] (:db-cmp mambobox.main/system))
