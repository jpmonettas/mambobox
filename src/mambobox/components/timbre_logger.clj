(ns mambobox.components.timbre-logger
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rolling :refer [make-rolling-appender]]
            [taoensso.timbre.appenders.postal :as postal-appender]))

(defn format-error-email [system-config output]
  (format "There was an ERROR in mambobox API
   error : %s \n 
 \n
  "
          output))

(defrecord TimbreLoggerComponent [system-config]
  component/Lifecycle

  (start [this]
    (println "Starting TimbreLoggerComponent")
    (timbre/set-config! [:appenders :spit :enabled?] true)
    (timbre/set-config! [:shared-appender-config :rolling] {:path (system-config :log-file-name)
                                                            :pattern :daily})
    (timbre/set-config! [:appenders :rolling-file] (make-rolling-appender {:enabled? true} {}))
    (timbre/set-config! [:appenders :postal]
                        (postal-appender/make-postal-appender
                         {:enabled?   (system-config :email-log-enabled)
                          :rate-limit [1 60000] ; 1 msg / 60,000 msecs (1 min)
                          :async?     true ; Don't block waiting for email to send
                          :min-level :error}
                         {:postal-config
                          ^{:host (system-config :email-log-smtp-server)
                            :user (system-config :email-log-smtp-user)
                            :pass (system-config :email-log-smtp-pass)
                            :ssl (system-config :email-log-smtp-ssl)}
                          {:from (system-config :email-log-from)
                           :to (system-config :email-log-to)}
                          :body-fn (fn [output] [{:type "text/plain; charset=utf-8"
                                                 :content (format-error-email system-config output)}])})))
  
  (stop [this]
    (println "Stopping TimbreLoggerComponent")
    this))

(defn create-timbre-logger-component [system-config]
  (map->TimbreLoggerComponent {:system-config system-config}))
