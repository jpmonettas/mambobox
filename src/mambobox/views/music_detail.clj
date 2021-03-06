(ns mambobox.views.music-detail
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))


;; (defn music-player [file-path]
;;   [:div
;;    [:input {:type "hidden" :id "song-file" :value (str "/files/" file-path)}]
;;    [:div {:id "jquery_jplayer_1" :class "jp-jplayer"}]
;;    [:div {:id "jp_container_1" :class "jp-audio"}
;;     [:div {:class "jp-type-single"}
;;      [:div {:class "jp-gui jp-interface"}
;;       [:ul {:class "jp-controls"}
;;        [:li [:a {:href "javascript:;" :class "jp-play" :tabindex "1"} "play"]]
;;        [:li [:a {:href "javascript:;" :class "jp-pause" :tabindex "1"} "pause"]]
;;        [:li [:a {:href "javascript:;" :class "jp-stop" :tabindex "1"} "stop"]]
;;        [:li [:a {:href "javascript:;" :class "jp-mute" :tabindex "1" :title "mute"} "mute"]]
;;        [:li [:a {:href "javascript:;" :class "jp-unmute" :tabindex "1" :title "unmute"} "unmute"]]]
;;       [:div {:class "jp-progress"}
;;        [:div {:class "jp-seek-bar"}
;;         [:div {:class "jp-play-bar"}]]]
;;       [:div {:class "jp-volume-bar"}
;;         [:div {:class "jp-volume-bar-value"}]]
;;             [:div {:class "jp-time-holder"}
;;              [:div {:class "jp-current-time"}]
;;              [:div {:class "jp-duration"}]
;;              [:ul {:class "jp-toggles"}
;;               [:li [:a {:href "javascrip:;" :class "jp-repeat" :tabindex "1" :title "repeat"} "repeat"]]
;;               [:li [:a {:href "javascrip:;" :class "jp-repeat-off" :tabindex "1" :title "repeat off"} "repeat off"]]]]]
;;      [:div {:class "jp-title"}
;;       [:ul [:li "Bubble"]]]
;;      [:div {:class "jp-no-solution"} "Navegador no soportado"]]]])

(defn music-player-min [file-path]
  [:div {:class "player"}
   [:input {:type "hidden" :id "song-file" :value (str "/files/" file-path)}]
   [:div {:id "jquery_jplayer_1" :class "jp-jplayer"}]
   [:div {:id "jp_container_1" :class "jp-audio"}
    [:div {:class "jp-type-single"}
     [:div {:class "jp-gui jp-interface"}
      [:ul {:class "jp-controls"}
       [:li [:a {:href "javascript:;" :class "jp-play" :tabindex "1"} "play"]]
       [:li [:a {:href "javascript:;" :class "jp-pause" :tabindex "1"} "pause"]]]
      [:div {:class "jp-progress"}
       [:div {:class "jp-seek-bar"}
        [:div {:class "jp-play-bar"}]]]
            [:div {:class "jp-time-holder"}
             [:div {:class "jp-current-time"}]
             [:div {:class "jp-duration"}]]]
     [:div {:class "jp-no-solution"} "Navegador no soportado"]]]])


(defn suggested-songs [songs]
  [:div {:class " col-md-4"}
   [:div {:class "sub-box"}
    [:div {:class "sub-box-title"} "Temas sugeridos"]
    [:div {:class "sub-box-content"}
     [:ol {:id "results-list"}
      (for [result songs
            :let [song-id (get result :_id)
                  song-name (get result :name)
                  artist (get result :artist)
                  tags (get result :tags)
                  video-links (get result :external-video-links)]]
        [:li {:class "result"}
         [:div {:class "container"}
          [:div {:class "row"}
           [:div {:class "song-name col-md-12 col-xs-12"} 
            [:a {:href (str "/music/" song-id)} [:span song-name]] (when-not (empty? video-links) [:i {:class "glyphicon glyphicon-facetime-video"}])]]
          [:div {:class "row"}
           [:div {:class "artist"} artist]]
          [:div {:class "row"}        
           [:div {:class "tags col-xs-9"}
            (for [tag tags]
              (gen/render-tag-label tag "search"))]]]])
      ]]]])

(defn song-details [song song-is-favourite]
  (let [song-id (get song :_id)
        song-name (get song :name)
        artist (get song :artist)
        uploader-username (get song :uploader-username)
        tags (get song :tags)
        file-path (get song :generated-file-name)]
    [:div {:id "main-music-detail-div" :class "col-md-8"}
     [:div {:class "sub-box"}
      [:div {:class "sub-box-title"} "Información del tema"]
      [:div {:class "sub-box-content"}

       [:form {:action (str "/music/" song-id) :method "POST"}
        
        [:div {:class "song-name input-group"}
         [:span {:class "input-group-addon"} "Tema:"]
         [:input {:type "text" :name "newsongname" :class "form-control input-lg" :value song-name :disabled ""}]
         [:span {:class "input-group-btn"}
          [:button {:class "btn btn-danger song-edit-button input-lg" :type "button"} [:i {:class "glyphicon glyphicon-pencil"} " "]]
          [:button {:class "btn btn-success input-lg" :type "submit" :style "display:none"} [:i {:class "glyphicon glyphicon-ok"} " "]]]]
        
        [:div {:class "artist input-group"}
         [:span {:class "input-group-addon"} "Artista:"]
         [:input {:type "text" :name "newartist" :class "form-control" :value artist :disabled ""}]
         [:span {:class "input-group-btn"}
          [:button {:class "btn btn-danger song-edit-button" :type "button"} [:i {:class "glyphicon glyphicon-pencil"} " "]]
          [:button {:class "btn btn-success" :type "submit" :style "display:none"} [:i {:class "glyphicon glyphicon-ok"} " "]]]]]
       [:input {:type "hidden" :id "song-id" :value song-id}]
       [:div {:class "soft-message"} (str "Subido por: " uploader-username)]
       [:div {:class "tags"}
        (for [tag tags]
          (gen/render-detail-tag-label tag))]
        [:div {:class "tags-box"}
         (gen/tag-filter-accordion "Agregar Tags" "detail" nil)]
       (when-not song-is-favourite
         [:div
          [:button {:class "btn btn-warning" :id "add-to-favourites"} [:i {:class "glyphicon glyphicon-star"}] "Favorito"]])
       (music-player-min file-path)]]]))


(defn external-related-videos [song]
  (let [links (get song :external-video-links)
        song-id (get song :_id)]
    [:div {:id "main-related-videos-div" :class "col-md-12 col-xs-12"}
     [:div {:class "sub-box"}
      [:div {:class "sub-box-title"} "Videos relacionados"]
      [:div {:class "sub-box-content clearfix"}
       (for [link links]
         [:div {:class "video-wrapper sub-box"} 
          [:div {:class "video-container "} 
           [:iframe {:type "text/html" :src link :frameborder "0"}]]
          [:input {:type "hidden" :value link}]
          [:button {:class "btn btn-danger delete-video-button"} "Quitar"]])
        [:form {:method "POST" :action (str "/music/" song-id "/links/")}
         [:div {:class "input-group"}
          [:input {:type "text" :class "form-control" :placeholder "Youtube link" :name "newlink"}]
          [:span {:class "input-group-btn"}
           [:button {:class "btn btn-info" :type "submit"} "Agregar"]]]]
       ]]]))
    

(defn music-detail-view [username song song-is-favourite sug-songs]
  (html5
    gen/head
    [:body
     [:div {:class "container"}
      [:div {:class "row"}
       gen/banner]
      [:div {:class "row"}
       (gen/navbar :music username)]
      [:div {:class "row"}
       (song-details song song-is-favourite)
       (suggested-songs sug-songs)
       ]
      [:div {:class "row"}
       (external-related-videos song)
       ]]
     (gen/footer-includes)]))
     

