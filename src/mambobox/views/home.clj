(ns mambobox.views.home
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))

(defn render-news [news]
  [:div {:id "news-main-div" :class "container"}
   (for [new news]
     (let [username (get new :username)
           text (get new :text)]
       [:div {:class "row"} 
        [:div {:class "new col-md-10 col-md-offset-1 col-xs-12"} 
         [:div {:class "username"} username]
         [:div {:class "text"} text]]]))
   [:form {:method "POST" :action "/news/"}
    [:div {:class "container"}
     [:div {:class "row"}
      [:textarea {:name "newtext" :class "col-md-8 col-md-offset-2 col-xs-12" :rows "5"}]]
     [:div {:class "row"}
      [:button {:class "btn btn-primary col-md-2 col-md-offset-8 col-xs-2 col-xs-offset-8" :type "submit"} "Agregar"]]]]])

      

(defn home-page-view [username news]
    (html5
      gen/head
      [:body
       [:div {:class "container"}
        [:div {:class "row"}
         gen/banner]
        [:div {:class "row"}
         (gen/navbar :home username)]
        [:div {:class "row"}
         [:div {:class "col-md-12 col-xs-12"}
          [:h3 "Noticias"]]
         [:div {:class "col-md-12 col-xs-12"}
          (render-news news)]]]
       (gen/footer-includes)]))
