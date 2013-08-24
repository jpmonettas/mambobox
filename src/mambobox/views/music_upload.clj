(ns mambobox.views.music-upload
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))


(defn file-select []
  [:div {:class "container"}
   [:div {:class "row"}
    [:div {:class "col-md-3 col-xs-6"}
     [:span {:class "btn btn-success fileinput-button"}
      [:i {:class "icon-plus icon-white"}]
      [:span "Seleccionar archivos"]
      [:input {:id "fileupload" :type "file" :name "files[]" :multiple ""}]]]
    [:div {:class "col-md-9 col-xs-5 col-xs-offset-4"}
     [:div {:id "progress" :class "progress"}
      [:div {:class "progress-bar progress-bar-success"}]]]]
   [:div {:class "row"}
    [:div {:id "files" :class "files"}]]])

   

(defn music-upload-view [username]
  (html5
    gen/head
    [:body
     [:div {:class "container"}
      [:div {:class "row"}
       gen/banner]
      [:div {:class "row"}
       (gen/navbar :music username)]
      [:div {:class "row"}
       (file-select)]]
     (gen/footer-includes)]))
