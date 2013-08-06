(ns mambobox.views.music-detail
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))


(defn song-details [song]
  (let [song-id (get song :_id)
        song-name (get song :name)
        artist (get song :artist)
        tags (get song :tags)
        file-path (get song :generated-file-name)]
       [:div
        [:audio {:controls ""}
         [:source {:src (str "/files/" file-path) :type "audio/mpeg"}]]
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
     
