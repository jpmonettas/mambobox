(ns mambobox.views.general
  (:use hiccup.core
        hiccup.def))

(defn bootstrap-incl []
  (html 
   [:link {:rel "stylesheet" :href "//netdna.bootstrapcdn.com/bootstrap/3.0.0-rc1/css/bootstrap.min.css"}]
   [:link {:rel "stylesheet" :href "/css/bootstrap-glyphicons.css"}]
   [:script {:src"//netdna.bootstrapcdn.com/bootstrap/3.0.0-rc1/js/bootstrap.min.js"}]))

(defn jquery-incl [] 
   [:script {:src "http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"}])

(defn jquery-file-upload-incl []
  (html
   [:link {:rel "stylesheet" :href "/css/jquery.fileupload-ui.css"}]
   [:script {:src "/js/vendor/jquery.ui.widget.js"}]
   [:script {:src "/js/jquery.iframe-transport.js"}]
   [:script {:src "/js/jquery.fileupload.js"}]))


(def head
  [:head
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   [:title "MamboBox"]
   [:link {:rel "stylesheet" :href "/css/mambo-styles.css"}]
   (jquery-incl)
   (bootstrap-incl)
   (jquery-file-upload-incl)
   [:script {:src "/js/mambobox.js"}]
   ])

(def banner
   [:div {:id "main-banner-div" :class "col-12"}
    [:div {:id "banner-text-div"} "MamboBox"]
    [:div {:id "logo-div"}]])

(defn navbar [active-tab]
  [:div {:class "navbar"}
   [:ul {:class "nav navbar-nav"}
    [:li (when (= active-tab :home) {:class "active"})  
     [:a {:href "/"} "Home"]]
    [:li (when (= active-tab :music) {:class "active"}) 
     [:a {:href "/music/"} "Music"]]]])

(def tags-color-map
  {"chacha" "#d9534f"
   "son" "#f0ad4e"
   "montuno" "#5bc0de"
   "mambo" "#999999"
   "rumba" "#5cb85c"
   "guaguanco" "#5cb85c"
   "yambu" "#5cb85c"
   "columbia" "#5cb85c"})

(defn render-tag-label [tag-name & icon]
  (let [tag-color (get tags-color-map tag-name)
        icon-name (first icon)]
    [:span {:class "label music-tag" :style (str "background-color:" tag-color)}
      tag-name (when icon [:i {:class (str "glyphicon " icon-name)}])]))
