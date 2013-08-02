(ns mambobox.views.views
  (:use hiccup.core)
  (:require [mambobox.data-access :as data]))

(defn test-view-in-views []
  (let [col (data/get-all-tests)]
    (html
     [:h1 "This is a test in views"]
     [:table
      (for [el col]
        [:tr
         [:td (get el :a)]
         [:td (get el :b)]])
      ])))
     