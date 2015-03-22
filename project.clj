(defproject mambobox "1.2.1-SNAPSHOT"
  :description "Mambobox"
  :url "http://www.mambobox.com.uy"
  :dependencies [[org.clojure/clojure "1.6.0"]

                 ;; Routing
                 ;;[compojure "1.3.2"]

                 ;; For uploaded file format recognition
                 [com.novemberain/pantomime "2.4.0"]
                 
                 ;; Website authentication/authorization
                 [com.cemerick/friend "0.2.1"]

                 [org.clojure/core.memoize "0.5.6"]

                 [org.clojure/core.cache "0.6.4"]
                 
                 ;; Html generation
                 [hiccup "1.0.5"]

                 ;; DB
                 [com.novemberain/monger "2.1.0"]

                 ;; Logging
                 [com.taoensso/timbre "3.4.0"]
                 [javax.mail/mail "1.4.7"]
                 [com.draines/postal "1.11.3"] ;; by email

                 ;; For managing staff that we need to initialize and have a lifecicle
                 [com.stuartsierra/component "0.2.3"]

                 
                 ;; Explring ID3 info
                 [org/jaudiotagger "2.0.3"]

                 ;; Fuzzy string matching for the search
                 [fuzzy-string "0.1.0-SNAPSHOT"]

                 ;; API
                 [org.clojure/data.json "0.2.2"]

                 ;; For naming files with it's checksum
                 [digest "1.3.0"]

                 ;; Exception handling
                 [slingshot "0.10.3"]

                 ;; Time
                 [clj-time "0.6.0"]

                 ;; Web server
                 [ring "1.3.2"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-codec "1.0.0"]


                 ;; To embeed a nrepl server in the app
                 [org.clojure/tools.nrepl "0.2.5"]
                 [cider/cider-nrepl "0.8.1"]

                 ;; For api with swagger
                 [metosin/compojure-api "0.18.0"]
                 [metosin/ring-swagger "0.18.1"]

                 [metosin/ring-http-response "0.6.0"]
                 [metosin/ring-swagger-ui "2.0.24"]
                 
                 [clj-stacktrace "0.2.5"]]
  :main mambobox.main
  :plugins [[lein-ring "0.8.6"]]
  :profiles {:dev {:dependencies [[ring-mock "0.1.5"]
                                  [javax.servlet/servlet-api "2.5"]                 

                                  ;; Debugging
                                  [org.clojure/tools.trace "0.7.8"]

                                  ;;Testing
                                  [midje "1.6.3"]
                                  [ring-mock "0.1.5"]]
                   :plugins [[lein-midje "3.1.1"]]}
             :prod {:global-vars {*assert* false}}
             :uberjar {:aot :all}})

