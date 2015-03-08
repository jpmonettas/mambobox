(ns mambobox.components.db
  (:require [com.stuartsierra.component :as component]
            [monger.core :as m]
            [taoensso.timbre :as l]))


(defrecord DbComponent
    [system-config]
  component/Lifecycle

  (start [this]
    (println (format "Starting DbComponent"))
    (assoc this :connection (m/connect)))
  (stop [this]
    (println "Stopping DbComponent")
    (m/disconnect (:connection this))
    this))

(defn get-db [db-cmp]
  (let [db-name (get-in db-cmp [:system-config :db-name])]
    (m/get-db (:connection db-cmp) db-name)))


(defn create-db-component [system-config]
  (map->DbComponent {:system-config system-config}))
