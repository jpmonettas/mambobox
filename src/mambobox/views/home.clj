(ns mambobox.views.home
  (:use hiccup.core
        hiccup.page
        mambobox.views.utils)
  (:require [mambobox.data-access :as data]))

(def head
  [:head
   [:title "MamboBox"]
   [:link {:rel "stylesheet" :href "/css/styles.css"}]
   (render-bootstrap-incl)
   ])

(def banner
   [:div {:id "main-banner-div" :class "col-12"}
    [:div {:id "banner-text-div"} "MamboBox"]
    [:div {:id "logo-div"}]])

(def navbar
  [:div {:class "navbar"}
   [:ul {:class "nav navbar-nav"}
    [:li  
     [:a {:href ""} "Home"]]
    [:li {:class "active"} 
     [:a {:href ""} "Music"]]]])

(def search-box
  [:div {:class "search-section"}
   [:div {:class "input-group"}
    [:span {:class "input-group-addon"} "Search:"]
    [:input {:type "text" :class "form-control" :placeholder "song name, artist name, etc"}]
    [:span {:class "input-group-btn"}
     [:button {:class "btn btn-default" :type "button"} "Go!"]]]])

(def result-col-example
  (list 
   {:song-name "Vente Negra",
    :artist "Habana con Kola",
    :tags ["son" "guaguanco"],
    :num-of-comments 6}
   {:song-name "Esperanza",
    :artist "Salsa Celtica",
    :tags ["chacha"],
    :num-of-comments 4}))


(defn search-results [result-col]
  [:div {:id "results-main-div"}
   [:ol {:id "results-list"}
    (for [result result-col
          :let [song-name (get result :song-name)
                artist (get result :artist)
                tags (get result :tags)]]
      [:li {:class "result"}
       [:div
        [:div {:class "song-name"} [:a {:href ""} song-name]]
        [:div {:class "artist"} artist]
        [:div {:class "tags"}
         (for [tag tags]
           [:span {:class (str "label" " " tag)} tag])
         ]]])
    ]
   (pagination 10 1)])



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

     

(defn music-page-view []
  (let [col (data/get-all-tests)]
    (html5
     [:html
      head
      [:body
       [:div {:class "container"}
        [:div {:class "row"}
         banner]
        [:div {:class "row"}
         navbar]
        [:div {:class "row"}
         [:div {:class "col-offset-3 col-6"}
          search-box]
         [:div
          [:a {:href "/music/upload"}
           [:button {:class "btn btn-primary" :type "button"} "Upload!"]]]]
        [:div {:class "row"}
         [:h3 {:class "col-4"} "Search results:"]]
        [:div {:class "row"}
         (search-results result-col-example)]
        ]]])))
     