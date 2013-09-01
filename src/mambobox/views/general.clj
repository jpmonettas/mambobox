(ns mambobox.views.general
  (:use hiccup.core
        hiccup.def)
  (:require [mambobox.utils :as utils]))

(def ordered-tags-array
  [["chacha" "#ff0000"]
   ["mambo" "#9303a7"]
   ["latin-jazz" "#993366"]
   ["guaracha" "#64a8d1"]
   ["salsa dura" "#2219b2"]
   ["romantica" "#cb0077"]
   ["bolero" "#e5399e"]
   ["pachanga" "#999900"]
   ["boogaloo" "#d9534f"]
   ["son" "#ff7800"]
   ["montuno" "#ff9a40"]
   ["songo" "#ffa700"]
   ["danzon" "#ffbd40"]
   ["rumba" "#138900"]
   ["guaguanco" "#389e28"]
   ["yambu" "#1dd300"]
   ["columbia" "#52e93a"]
   ["afro" "#a64b00"]])

;; Just for fast searching
(def tags-color-map (into {} ordered-tags-array))


(defn head-includes []
  (html
   [:link {:rel "stylesheet" :href "/css/jquery.fileupload-ui.css"}] 
   [:link {:rel "stylesheet" :href "/css/bootstrap.min.css"}]
   [:link {:rel "stylesheet" :href "/skin/jplayer.blue.monday-mine.css"}]
   [:link {:rel "stylesheet" :href "/css/bootstrap-glyphicons.css"}]
   [:link {:rel "stylesheet" :href "/css/mambo-styles.css"}]))

(defn footer-includes []
  (html
   [:script {:src "http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"}]
   [:script {:src "/js/vendor/jquery.ui.widget.js"}]
   [:script {:src "http://blueimp.github.io/JavaScript-Load-Image/js/load-image.min.js"}]
   [:script {:src "/js/jquery.iframe-transport.js"}]
   [:script {:src "/js/jquery.fileupload.js"}]
   [:script {:src "/js/jquery.fileupload-process.js"}]
   [:script {:src "/js/jquery.fileupload-image.js"}]
   [:script {:src "/js/jquery.fileupload-audio.js"}]
   [:script {:src "/js/jquery.fileupload-validate.js"}]
   [:script {:src "/js/jquery.jplayer.min.js"}]
   [:script {:src "/js/bootstrap.min.js"}]
   [:script {:src "/js/mambobox.js"}]))

(def head
  [:head
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   [:title "MamboBox"]
   (head-includes)])

(def banner
   [:div {:id "main-banner-div" :class "col-12"}
    [:div {:id "banner-text-div"} "MamboBox"]
    [:div {:id "logo-div"}]])

(defn render-all-tags [div-extra-class]
  (html 
   (for [[tag color] ordered-tags-array]
     [:div {:class "label-wrapper-div"}
      [:span {:class (str "label music-tag " div-extra-class) :style (str "background-color:" color)} tag]])))

(defn tag-filter-accordion [title extra-class]
  [:div {:class "accordion" :id "tags-accordion"}
   [:div {:class "panel-group"}
    [:div {:class "panel panel-default"}
     [:div {:class "panel-heading"}
      [:div {:class "panel-title"}
       [:a {:class "accordion-toggle" :data-toggle "collapse" :data-parent "#tags-accordion" :href "#collapse"} title]]]
    [:div {:id "collapse" :class "panel-collapse collapse"}
     [:div {:class "panel-body"}
      [:div {:class "clearfix tags-container"}
              (render-all-tags extra-class)]]]]]])

(defn navbar [active-tab username]
  [:nav {:class "navbar navbar-default" :role "navigation"}
   [:div {:class "navbar-header"}
    [:button {:type "button" :class "navbar-toggle" :data-toggle "collapse" :data-target ".navbar-ex1-collapse"}
     [:span {:class "sr-only"} "Toggle navigation"]
     [:span {:class "icon-bar"}]
     [:span {:class "icon-bar"}]
     [:span {:class "icon-bar"}]]
    [:span {:class "navbar-brand"} (utils/get-name-from-email username)]]
   [:div {:class "collapse navbar-collapse navbar-ex1-collapse"}
    [:ul {:class "nav navbar-nav"}
     [:li (when (= active-tab :home) {:class "active"})  
      [:a {:href "/"} "Inicio"]]
     [:li {:class (str "dropdown " (when (= active-tab :music) "active"))} 
           [:a {:href "#" :class "dropdown-toggle" :data-toggle "dropdown"} "Musica" [:b {:class "caret"}]]
           [:ul {:class "dropdown-menu"}
            [:li 
             [:a {:href "/music/"} "Buscar"]]        
            [:li 
             [:a {:href "/upload"} "Subir"]]]]]
    [:ul {:class "nav navbar-nav navbar-right"}
     [:li [:a {:href "/logout"} [:i {:class "glyphicon glyphicon-log-out"}] "Salir" ]]]]])
    

(defn render-detail-tag-label [tag-name]
  (let [tag-color (get tags-color-map tag-name)]
    [:div {:class "btn-group detail-tag"}
     [:span {:class "label music-tag dropdown-toggle" 
               :style (str "background-color:" tag-color)
               :data-toggle "dropdown"} tag-name]
     [:ul {:class "dropdown-menu"}
      [:li "Quitar"]]]))

(defn render-tag-label [tag-name extra-class & icon]
  (let [tag-color (get tags-color-map tag-name)
        icon-name (first icon)]
    [:span {:class (str "label music-tag " extra-class) :style (str "background-color:" tag-color)}
      tag-name (when icon [:i {:class (str "glyphicon " icon-name)}])]))

