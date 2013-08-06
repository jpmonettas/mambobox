(ns mambobox.views.music-search
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))


(defn search-box [q]
  [:div {:class "search-section"}
   [:form {:method "GET" :action "/music/"}
    [:div {:class "input-group"}
     [:span {:class "input-group-addon"} "Search:"]
     [:input {:type "text" :name "q" :value q :class "form-control" :placeholder "song name, artist name, etc"}]
     [:span {:class "input-group-btn"}
      [:button {:class "btn btn-default" :type "submit"} "Go!"]]]]])


(defn pagination [num-pages cur-page]
  [:div {:id "pagination-div"}
   [:ul {:class "pagination"}
    [:li (when (= cur-page 1) {:class "disabled"}) 
     [:span {:class "left-arrow"} "&laquo;"]]
    (for [i (range 1 (+ num-pages 1))]
      [:li (if (= cur-page i) {:class "active page-link"} {:class "page-link"})
       [:span i]])
    [:li (when (= cur-page num-pages) {:class "disabled"}) 
     [:span {:class "right-arrow"} "&raquo;"]]]])

(defn search-results [result-col cur-page num-pages]
  [:div {:id "results-main-div"}
   [:ol {:id "results-list"}
    (for [result result-col
          :let [song-id (get result :_id)
                song-name (get result :name)
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
   (pagination num-pages cur-page)])


(defn music-search-view [result-col q cur-page num-pages]
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
          (search-box q)]
         [:div
          [:a {:href "/upload"}
           [:button {:class "btn btn-primary" :type "button"} "Upload!"]]]]
        [:div {:class "row"}
         [:h3 {:class "col-4"} "Search results:"]]
        [:div {:class "row"}
         (search-results result-col cur-page num-pages)]
        ]]]))
     
