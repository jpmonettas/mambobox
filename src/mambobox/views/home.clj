(ns mambobox.views.home
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))


(defn home-page-view []
    (html5
      gen/head
      [:body
       [:div {:class "container"}
        [:div {:class "row"}
         gen/banner]
        [:div {:class "row"}
         (gen/navbar :home)]
        [:div {:class "row"}
         [:span {:class "glyphicon glyphicon-user"}]]]]))
