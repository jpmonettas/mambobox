(ns mambobox.logger
  (:use clj-logging-config.log4j
        [clojure.string :only [lower-case trim]])
  (:require [clojure.tools.logging :as log]))


(let [environment (trim (lower-case (System/getenv "MAMBOBOX_ENV")))]
  (cond (= environment "prod")
        (set-loggers! "mambobox"
                      {:level :debug
                       :out (org.apache.log4j.DailyRollingFileAppender.
                             (org.apache.log4j.EnhancedPatternLayout. "%d %p [%t] %c %m%n")
                             "./mambobox.log"
                             "yyyy-MM-dd")})
        :else
        (set-loggers! "mambobox"
                      {:level :debug
                       :pattern "%d %p [%t] %c %m%n"}))
  (log/info "Setting logger for environment : [" environment "]"))

             


