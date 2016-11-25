(ns mambobox.components.web-server
  (:require [mambobox.routes :as r]
            [mambobox.api-routes :as api-r]
            [com.stuartsierra.component :as component]
            [compojure.core :as cc]
            [compojure.route :as cr]
            [ring.adapter.jetty :as rj]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.util.codec :as ru]
            [ring.util.response :as ring-response]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as l]
            [mambobox.utils :as utils]
            [cemerick.friend :as friend]
            [mambobox.data-access :as data]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))


(defn wrap-components
  "Inject all components in the request"
  
  [handler system-config db-cmp]
  (fn [request]
    (let [injected-request (-> request
                              (assoc :system-config system-config)
                              (assoc :db-cmp db-cmp))]
      (handler injected-request))))


(defn wrap-debug
  "Log debug the entire request"
  [handler]
  (fn [request]
    (handler (l/spy request))))

(defn wrap-log-exception [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (l/error e)))))


(defn create-app-handler [system-config db-cmp]
  (-> (cc/routes

      (cr/resources "/")
      (cr/files "/files/" {:root (:music-dir system-config)})

      ;; Api routes
      api-r/api-routes
      
      ;; Site routes
      r/app-routes

      (friend/authenticate r/app-auth-routes 
                           {:allow-anon? nil
                            :login-uri "/login"
                            :credential-fn (partial creds/bcrypt-credential-fn
                                                    (partial data/get-user-by-username db-cmp))
                            :workflows [(workflows/interactive-form)]})

      (cr/not-found "Not Found"))
     (wrap-components system-config db-cmp)
     (wrap-session)
     (wrap-multipart-params)
     (wrap-keyword-params)
     (wrap-nested-params)
     (wrap-params)
     (utils/wrap-mp3-files-contentype)
     (utils/wrap-my-exception-logger)))

(defrecord WebServerComponent
    [system-config timbre-logger-cmp db-cmp]
  component/Lifecycle

  (start [this]
    (let [port (get-in this [:system-config :web-server-port])]
      (println (format "Starting WebServerComponent on port %s" port))
      (assoc this :jetty (rj/run-jetty
                          (create-app-handler system-config db-cmp)
                          {:port port
                           :join? false}))))
  (stop [this]
    (println "Stopping WebServerComponent")
    (.stop (:jetty this))
    this))

(defn create-web-server-component [system-config]
  (map->WebServerComponent {:system-config system-config}))
