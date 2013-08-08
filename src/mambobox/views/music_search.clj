(ns mambobox.views.music-search
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))


(defn search-box [q tag]
  [:div {:class "search-section"}
   [:form {:method "GET" :action "/music/"}
    [:div {:class "input-group"}
     [:input {:type "text" :name "q" :value q :class "form-control" :placeholder "song name, artist name, etc"}]
     [:input {:type "hidden" :name "tagfilter" :value tag :id "tag-filter"}]
     [:span {:class "input-group-btn"}
      [:button {:class "btn btn-default" :type "submit"} "Go!"]]]]])

(defn tag-filter-accordion []
  [:div {:class "accordion" :id "tag-filter-accordion"}
   [:div {:class "accordion-group"}
    [:div {:class "accordion-heading"}
     [:a {:class "accordion-toggle" :data-toggle "collapse" :data-parent "#tag-filter-accordion" :href "#collapse"} "Tag filter"]]
    [:div {:id "collapse" :class "accordion-body collapse"}
     [:div {:class "accordion-inner"}
      [:div {:class "clearfix tags-container"}
       (for [[tag color] gen/tags-color-map]
         [:div {:class "label-wrapper-div"}
          [:span {:class "label music-tag" :style (str "background-color:" color)} tag]])]]]]])

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
   (tag-filter-accordion)
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
           (gen/render-tag-label tag))
         ]]])
    ]
   (pagination num-pages cur-page)])


(defn music-search-view [result-col q tag cur-page num-pages]
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
         [:div {:class "col-8 col-offset-2"}
          (search-box q tag)]
         [:div
          [:a {:href "/upload"}
           [:button {:class "btn btn-primary" :type "button"} "Upload!"]]]]
        [:div {:class "row"}
         [:div {:class "col-8 col-lg-2"}
          [:h3  "Search results:"]]
         [:div {:class "col-3 col-lg-1 tag-search"}
          (when tag (gen/render-tag-label tag "glyphicon-remove"))]]
        [:div {:class "row"}
         (search-results result-col cur-page num-pages)]
        ]]]))
     
