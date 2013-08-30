(defproject mambobox "0.2.0-SNAPSHOT"
  :description "Mambobox"
  :url "http://www.mambobox.com.uy"
  :dependencies [[org.clojure/clojure "1.5.1"]                 
                 [com.cemerick/friend "0.2.0-SNAPSHOT"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.4"]
                 [com.novemberain/monger "1.5.0"]
                 [org.clojure/tools.logging "0.2.6"]
                 [log4j/log4j "1.2.16"]
                 [org/jaudiotagger "2.0.3"]
                 [fuzzy-string "0.1.0-SNAPSHOT"]
                 [org.clojure/data.json "0.2.2"]
                 [digest "1.3.0"]
                 [slingshot "0.10.3"]
                 [clj-time "0.6.0"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [clj-logging-config "1.9.10"]
                 [org.clojure/tools.nrepl "0.2.3"]]
  :main mambobox.handler
  :plugins [[lein-ring "0.8.6"]]
  :ring {:handler mambobox.handler/app
         :open-browser? false}
  :profiles {:production 
             {:ring
              {:stacktraces? false,
               :auto-reload? false}}
             :dev 
             {:ring
              {:auto-reload? true
               :auto-refresh true
               :stacktraces? false}
              :dependencies [[ring-mock "0.1.5"]]}})

