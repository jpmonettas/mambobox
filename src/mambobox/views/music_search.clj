(ns mambobox.views.music-search
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))


(defn search-box [q tag collection-filter]
  [:div {:class "search-section container"}
   [:div {:class "row"}
    [:form {:method "GET" :action "/music/" :id "search-form"}
     [:div {:class "col-md-6 col-md-offset-2 col-xs-12"  :id "search-input-div"}
      [:div {:class "input-group"}
       [:input {:type "text" :name "q" :value q :class "form-control" :placeholder "tema, artista, etc"}]
       [:input {:type "hidden" :name "tagfilter" :value tag :id "tag-filter"}]
       [:span {:class "input-group-btn"}
        [:button {:class "btn btn-primary" :type "submit"} "Ir!"]]]]
     [:div {:class "col-md-3 col-xs-12" :id "collection-filter-div"}
      [:div {:class "btn-group" :data-toggle "buttons"}
       [:label {:class (str "btn btn-primary " (when (= collection-filter "all") "active"))}
        [:input (merge {:type "radio" :name "collection-filter" :value "all"}
                       (when (= collection-filter "all") {:checked ""})) "Todo"]]
       [:label {:class (str "btn btn-primary " (when (= collection-filter "favourites") "active"))}
        [:input (merge {:type "radio" :name "collection-filter" :value "favourites"}
                       (when (= collection-filter "favourites") {:checked ""})) "Mis Favoritos"]]]]]]])




(defn pagination [num-pages cur-page]
  [:div {:id "pagination-div"}
   [:ul {:class "pagination"}
    [:li (when (= cur-page 1) {:class "disabled"}) 
     [:span {:class "left-arrow"} "&laquo;"]]
    (let [first-page (if (<= cur-page 3)
                      1
                      (- cur-page 2))
          last-page (if (<= (+ first-page 4) num-pages)
                      (+ first-page 5)
                      (+ num-pages 1))]
      (for [i (range first-page last-page)]
        [:li (if (= cur-page i) {:class "active page-link"} {:class "page-link"})
         [:span i]]))
    [:li (when (= cur-page num-pages) {:class "disabled"}) 
     [:span {:class "right-arrow"} "&raquo;"]]]])


(defn search-results [result-col cur-page num-pages tags-freaq-map favs]
  (if-not (empty? result-col)
    [:div {:id "results-main-div" :class "col-md-12 col-xs-12"}
     (gen/tag-filter-accordion "Filtrar por etiqueta" "search" tags-freaq-map)
     [:ol {:id "results-list"}
      (for [result result-col
            :let [song-id (get result :_id)
                  song-name (get result :name)
                  artist (get result :artist)
                  visits (get result :visits)
                  tags (get result :tags)
                  video-links (get result :external-video-links)]]
        [:li {:class "result"}
         [:div {:class "container"}
          [:div {:class "row"}
           [:div {:class "song-name col-md-12 col-xs-12"} 
            [:a {:href (str "/music/" song-id)} [:span song-name]] (when-not (empty? video-links) [:i {:class "glyphicon glyphicon-facetime-video"}])]]
          [:div {:class "row"}
           [:div {:class "artist col-md-10 col-xs-12"} artist]
           [:div {:class "song-visits col-md-2 col-xs-12"} (str "Visto " visits " veces")]]
          [:div {:class "row"}        
           [:div {:class "tags col-xs-9"}
            (for [tag tags]
              (gen/render-tag-label tag "search"))]
           [:input {:type "hidden" :name "song-id" :value song-id}]
           (when favs [:button {:class "btn btn-warning col-md-1 col-md-offset-2 remove-fav-btn"} "Quitar"])]]])
      ]
     (pagination num-pages cur-page)]
    [:div {:id "results-main-div" :class "col-md-12 col-xs-12"}
     "No se encontrarons resultados para su busqueda"]))


(defn music-search-view [username result-col q tag collection-filter cur-page num-pages tags-freaq-map favs]
    (html5
      gen/head
      [:body
       [:div {:class "container"}
        [:div {:class "row"}
         gen/banner]
        [:div {:class "row"}
         (gen/navbar :music username)]
        [:div {:class "row"}
         [:div {:class "col-md-12 col-xs-12"}
          (search-box q tag collection-filter)]]
        (when (or 
               (not (empty? q))
               (not (empty? tag)))
          [:div {:class "row"}
           [:div {:class "col-md-2"}
            [:h3  "Resultados:"]]
           [:div {:class "col-md-1 tag-search"}
            (when (not (empty? tag)) (gen/render-tag-label tag "remove" "glyphicon-remove"))]])
        [:div {:class "row"}
         (search-results result-col cur-page num-pages tags-freaq-map favs)]]
       (gen/footer-includes)]))
     
