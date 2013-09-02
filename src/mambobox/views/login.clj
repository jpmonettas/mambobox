(ns mambobox.views.login
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.views.general :as gen]))


(defn login 
  ([] (login nil))
  ([username] 
     (html5
      gen/head
      [:body
       [:div {:class "container" :id "login-main-div"}
        [:div {:class "row"}
         [:div {:class "col-lg-6 col-lg-offset-3 col-12"}
          [:div {:class "sub-box"}
           [:div {:class "sub-box-title"} "Mambobox Login"]
           [:div {:class "sub-box-content"}
            [:div {}
             (when username [:div {:class "alert alert-success"} "Usuario creado con exito"])
             [:form {:class "form-horizontal" :method "POST" :action "/login"}
              [:div {:class "form-group"}
               [:label {:for "inputEmail" :class "col-lg-2 control-label"} "Email"]
               [:div {:class "col-lg-10"}
                [:input {:type "text" :class "form-control" :id "inputEmail" :placeholder "Ej : mambo@gmail.com" :name "username" :value username}]]]
              [:div {:class "form-group"}
               [:label {:for "inputPassword" :class "col-lg-2 control-label"} "Contraseña"]
               [:div {:class "col-lg-10"}
                [:input {:type "password" :class "form-control" :id "inputPassword" :name "password"}]]]
              [:div {:class "form-group"}
               [:div {:class "col-md-offset-5 col-md-2 col-xs-offset-5 col-xs-7"}
                [:button {:type "submit" :class "btn btn-primary"} "Entrar"]]
               [:div {:class "col-md-offset-2 col-md-2 col-xs-offset-5 col-xs-5 registration"}
                [:a {:href "/users/"} "Registrate"]]]]]]]]]]
       (gen/footer-includes)])))


