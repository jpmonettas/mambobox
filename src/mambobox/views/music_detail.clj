(ns mambobox.views.music-detail
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))


                    
(def select-tag-modal 
  [:div {:class "modal fade" :id "select-tag-modal"}
   [:div {:class "modal-dialog"}
    [:div {:class "modal-content"}
     [:div {:class "modal-header"}
      [:button {:type "button" :class "close" :data-dismiss "modal" :aria-hidden "true"} "&times;"]
      [:h4 {:class "modal-title"} "Select tag"]]
     [:div {:class "modal-body"} 
      [:div {:id "tags-container" :class "tags-container clearfix"}
       (for [[tag color] gen/tags-color-map]
         [:div {:class "label-wrapper-div"}
          [:span {:class "label music-tag select" :style (str "background-color:" color)} tag]])]
      [:div {:id "selected-tag-div"}
       [:span  "Selected tag"] [:div {:id "selected-tag"} "None"]]]
     [:div {:class "modal-footer"}
      [:button {:type "button" :class "btn btn-default" :data-dismiss "modal"} "Close"]
      [:button {:type "button" :class "btn btn-primary" :id "add-tag"} "Add"]]]]])

(defn edit-song-modal [song-id song-name artist]
  [:form {:action (str "/music/" song-id) :method "POST"}
   [:div {:class "modal fade" :id "edit-song-modal"}
    [:div {:class "modal-dialog"}
     [:div {:class "modal-content"}
      [:div {:class "modal-header"}
       [:button {:type "button" :class "close" :data-dismiss "modal" :aria-hidden "true"} "&times;"]
       [:h4 {:class "modal-title"} "Edit song"]]
      [:div {:class "modal-body"} 
       [:div {:class "song-name input-group"}
        [:span {:class "input-group-addon"} "Song:"]
        [:input {:type "text" :name "newsongname" :class "form-control" :value song-name}]]
       [:div {:class "artist input-group"}
        [:span {:class "input-group-addon"} "Artist:"]
        [:input {:type "text" :name "newartist" :class "form-control" :value artist}]]]
      [:div {:class "modal-footer"}
       [:button {:type "button" :class "btn btn-default" :data-dismiss "modal"} "Close"]
       [:button {:type "submit" :class "btn btn-primary" :id "edit-song-ok"} "Ok"]]]]]])

(defn song-details [song]
  (let [song-id (get song :_id)
        song-name (get song :name)
        artist (get song :artist)
        tags (get song :tags)
        original-file-name (get song :original-file-name)
        file-path (get song :generated-file-name)]
    [:div {:id "main-music-detail-div" :class "col-12"}
     [:div {:class "sub-box"}
      [:div {:class "sub-box-title"} "Song Info"]
      [:div {:class "sub-box-content"}
       [:input {:type "hidden" :id "song-id" :value song-id}]
       [:div {:class "song-name"} song-name [:a {:data-toggle "modal" :href "#edit-song-modal" :class "btn btn-danger btn-xs"} 
                                             [:i {:class "glyphicon glyphicon-pencil"} " "]]]
       (edit-song-modal song-id song-name artist)
       [:div {:class "artist"} artist]
       [:div {:class "original-file-name"} (str "Original file: " original-file-name)]
       [:div {:class "tags"}
        (for [tag tags]
          (gen/render-detail-tag-label tag))
        [:a {:data-toggle "modal" :href "#select-tag-modal" :class "btn btn-primary btn-xs"} 
         [:i {:class "glyphicon glyphicon-tags"} " "]]
        select-tag-modal
        ]
       [:audio {:controls ""}
        [:source {:src (str "/files/" file-path) :type "audio/mpeg"}]]]]]))

(defn external-related-videos [song]
  (let [links (get song :external-video-links)
        song-id (get song :_id)]
    [:div {:id "main-related-videos-div" :class "col-12"}
     [:div {:class "sub-box"}
      [:div {:class "sub-box-title"} "Improvisations and Choreos"]
      [:div {:class "sub-box-content clearfix"}
       (for [link links]
         [:div {:class "video-wrapper sub-box"} 
          [:div {:class "video-container "} 
           [:iframe {:type "text/html" :src link :frameborder "0"}]]
          [:input {:type "hidden" :value link}]
          [:button {:class "btn btn-danger delete-video-button"} "Remove"]])
        [:form {:method "POST" :action (str "/music/" song-id "/links/")}
         [:div {:class "input-group"}
          [:input {:type "text" :class "form-control" :placeholder "youtube link" :name "newlink"}]
          [:span {:class "input-group-btn"}
           [:button {:class "btn btn-info" :type "submit"} "Add"]]]]
       ]]]))
    

(defn music-detail-view [song]
  (html5
    gen/head
    [:body
     [:div {:class "container"}
      [:div {:class "row"}
       gen/banner]
      [:div {:class "row"}
       (gen/navbar :music)]
      [:div {:class "row"}
       (song-details song)
       ]
      [:div {:class "row"}
       (external-related-videos song)
       ]]]))
     

