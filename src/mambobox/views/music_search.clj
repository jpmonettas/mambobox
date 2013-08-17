(ns mambobox.views.music-search
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))


(defn search-box [q tag]
  [:div {:class "search-section"}
   [:form {:method "GET" :action "/music/"}
    [:div {:class "input-group"}
     [:input {:type "text" :name "q" :value q :class "form-control" :placeholder "tema, artista, etc"}]
     [:input {:type "hidden" :name "tagfilter" :value tag :id "tag-filter"}]
     [:span {:class "input-group-btn"}
      [:button {:class "btn btn-primary" :type "submit"} "Ir!"]]]]])

(defn tag-filter-accordion []
  [:div {:class "accordion" :id "tag-filter-accordion"}
   [:div {:class "accordion-group"}
    [:div {:class "accordion-heading"}
     [:a {:class "accordion-toggle" :data-toggle "collapse" :data-parent "#tag-filter-accordion" :href "#collapse"} "Filtrar por etiqueta"]]
    [:div {:id "collapse" :class "accordion-body collapse"}
     [:div {:class "accordion-inner"}
      [:div {:class "clearfix tags-container"}
              (gen/render-all-tags "search")]]]]])

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
  [:div {:id "results-main-div" :class "col-md-10 col-xs-12"}
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
           (gen/render-tag-label tag "search"))
         ]]])
    ]
   (pagination num-pages cur-page)])


(defn music-search-view [username result-col q tag cur-page num-pages]
    (html5
      gen/head
      [:body
       [:div {:class "container"}
        [:div {:class "row"}
         gen/banner]
        [:div {:class "row"}
         (gen/navbar :music username)]
        [:div {:class "row"}
         [:div {:class "col-md-6 col-md-offset-3 col-xs-12"}
          (search-box q tag)]]
        (when (or 
               (not (empty? q))
               (not (empty? tag)))
          [:div {:class "row"}
           [:div {:class "col-md-2"}
            [:h3  "Resultados:"]]
           [:div {:class "col-md-1 tag-search"}
            (when (not (empty? tag)) (gen/render-tag-label tag "remove" "glyphicon-remove"))]])
        [:div {:class "row"}
         (search-results result-col cur-page num-pages)]]
       (gen/footer-includes)]))
     
