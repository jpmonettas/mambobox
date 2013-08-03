(ns mambobox.views.music-detail
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))

;; [:button {:class "btn"} [:i {:class "glyphicon glyphicon-plus"}]]

(defn song-details [song]
  (let [song-id (get song :id)
        song-name (get song :song-name)
        artist (get song :artist)
        tags (get song :tags)]
       [:div
        [:div {:class "song-name"} song-name]
        [:div {:class "artist"} artist]
        [:div {:class "tags"}
         (for [tag tags]
           [:span {:class (str "label music-tag" " " tag)} tag])
         ]]))


(defn music-detail-view [song]
    (html5
     [:html
      gen/head
      [:body
       [:div {:class "container"}
        [:div {:class "row"}
         gen/banner]
        [:div {:class "row"}
         (gen/navbar :music)]
        [:div {:class "row"}
         (song-details song)
         ]]]]))
     