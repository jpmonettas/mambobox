(ns mambobox.views.utils
  (:use hiccup.core
        hiccup.def))

(defhtml render-bootstrap-incl []
   [:link {:rel "stylesheet" :href "//netdna.bootstrapcdn.com/bootstrap/3.0.0-rc1/css/bootstrap.min.css"}]
   [:script {:src"//netdna.bootstrapcdn.com/bootstrap/3.0.0-rc1/js/bootstrap.min.js"}])