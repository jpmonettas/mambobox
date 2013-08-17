(ns mambobox.views.music-upload
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))


(defn file-select []
  [:div
   [:div {:class "row"}
    [:div  {:class "col-offset-5 col-7 upload-button-div"}
    [:span {:class "btn btn-success fileinput-button"}
     [:i {:class "icon-plus icon-white"}]
     [:span "Selecciona archivo"]
     [:input {:id "fileupload" :type "file" :name "files" :data-url "/upload" :multiple nil}]]]]
   [:div {:class "row"}
    [:div  {:class "col-offset-1 col-10"}
     [:div {:id "progress" :class "progress progress-success progress-striped"}
      [:div {:class "bar"}]]]]])

(defn file-select2 []
  [:div
   [:span {:class "btn btn-success fileinput-button"}
    [:i {:class "icon-plus icon-white"}]
    [:span "Add files..."]
    [:input {:id "fileupload" :type "file" :name "files[]" :multiple ""}]]
   [:br]
   [:br]
   [:div {:id "progress" :class "progress"}
    [:div {:class "progress-bar progress-bar-success"}]]
   [:div {:id "files" :class "files"}]
   [:br]])
   

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
       (file-select2)
       ]]
     (gen/footer-includes)]))
