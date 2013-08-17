(defproject mambobox "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.cemerick/friend "0.2.0-SNAPSHOT"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.4"]
                 [com.novemberain/monger "1.5.0"]
                 [org.clojure/tools.logging "0.2.6"]
                 [log4j/log4j "1.2.16"]
                 [org/jaudiotagger "2.0.3"]]
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler mambobox.handler/app
         :auto-reload? true
         :auto-refresh true
         :nrepl {:start? true :port 7777}}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
