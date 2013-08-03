(ns mambobox.views.general
  (:use hiccup.core
        hiccup.def))

(defn bootstrap-incl []
  (html 
   [:link {:rel "stylesheet" :href "//netdna.bootstrapcdn.com/bootstrap/3.0.0-rc1/css/bootstrap.min.css"}]
   [:link {:rel "stylesheet" :href "/css/bootstrap-glyphicons.css"}]
   [:script {:src"//netdna.bootstrapcdn.com/bootstrap/3.0.0-rc1/js/bootstrap.min.js"}]))

(def head
  [:head
   [:title "MamboBox"]
   [:link {:rel "stylesheet" :href "/css/styles.css"}]
   (bootstrap-incl)
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
