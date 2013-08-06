(ns mambobox.views.music-upload
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))


(defn file-select []
  [:div
   [:span {:class "btn btn-success fileinput-button"}
    [:i {:class "icon-plus icon-white"}]
    [:span "Select Files..."]
    [:input {:id "fileupload" :type "file" :name "files" :data-url "/upload" :multiple nil}]]
   [:br]
   [:br]
   [:div {:id "progress" :class "progress progress-success progress-striped"}
    [:div {:class "bar"}]]])

(defn music-upload-view []
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
       (file-select)
       ]]]]))