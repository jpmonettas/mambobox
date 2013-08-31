(ns mambobox.config
  (:use clj-logging-config.log4j
        [clojure.string :only [lower-case trim]])
  (:require [clojure.tools.logging :as log]))

(def music-dir (System/getenv "MAMBOBOX_MUSIC_DIR"))

(def result-page-size 10)

(let [environment (trim (lower-case (System/getenv "MAMBOBOX_ENV")))]
  (cond (= environment "prod")
        (set-loggers! "mambobox"
                      {:level :debug
                       :out (org.apache.log4j.DailyRollingFileAppender.
                             (org.apache.log4j.EnhancedPatternLayout. "%d %p [%t] %c %m%n")
                             "/home/mambobox/logs/mambobox.log"
                             "yyyy-MM-dd")})
        :else
        (set-loggers! "mambobox"
                      {:level :debug
                       :pattern "%d %p [%t] %c %m%n"}))
  (log/info "Setting logger for environment : [" environment "]"))

             


