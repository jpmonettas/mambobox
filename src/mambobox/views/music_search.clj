(ns mambobox.views.music-search
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))


(def search-box
  [:div {:class "search-section"}
   [:div {:class "input-group"}
    [:span {:class "input-group-addon"} "Search:"]
    [:input {:type "text" :class "form-control" :placeholder "song name, artist name, etc"}]
    [:span {:class "input-group-btn"}
     [:button {:class "btn btn-default" :type "button"} "Go!"]]]])


(defn pagination [num-pages cur-page]
  [:div {:id "pagination-div"}
   [:ul {:class "pagination"}
    [:li (when (= cur-page 1) {:class "disabled"}) 
     [:a {:href ""} "&laquo;"]]
    (for [i (range 1 (+ num-pages 1))]
      [:li (when (= cur-page i) {:class "active"})
       [:span i]])
    [:li (when (= cur-page num-pages) {:class "disabled"}) 
     [:a {:href ""} "&raquo;"]]]])

(defn search-results [result-col]
  [:div {:id "results-main-div"}
   [:ol {:id "results-list"}
    (for [result result-col
          :let [song-id (get result :id)
                song-name (get result :song-name)
                artist (get result :artist)
                tags (get result :tags)]]
      [:li {:class "result"}
       [:div
        [:div {:class "song-name"} [:a {:href (str "/music/" song-id)} song-name]]
        [:div {:class "artist"} artist]
        [:div {:class "tags"}
         (for [tag tags]
           [:span {:class (str "label music-tag" " " tag)} tag])
         ]]])
    ]
   (pagination 10 1)])


(defn music-search-view [result-col]
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
         [:div {:class "col-offset-3 col-6"}
          search-box]
         [:div
          [:a {:href "/music/upload"}
           [:button {:class "btn btn-primary" :type "button"} "Upload!"]]]]
        [:div {:class "row"}
         [:h3 {:class "col-4"} "Search results:"]]
        [:div {:class "row"}
         (search-results result-col)]
        ]]]))
     