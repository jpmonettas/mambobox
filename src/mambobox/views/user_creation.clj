(ns mambobox.views.user-creation
  (:use hiccup.core
        hiccup.page)
  (:require [mambobox.data-access :as data]
            [mambobox.views.general :as gen]))

(defn create-user-view 
  ([] (create-user-view nil nil nil nil)) 
  ([errors first-name last-name username] 
     (html5
      gen/head
      [:body
       [:div {:class "container" :id "user-create-main-div"}
        [:div {:class "row"}
         [:div {:class "col-lg-6 col-lg-offset-3 col-12"}
          [:div {:class "sub-box"}
           [:div {:class "sub-box-title"} "Registrate en Mambobox"]
           [:div {:class "sub-box-content"}
            [:div {}
             [:form {:class "form-horizontal" :method "POST" :action "/users/"}

              [:div {:class "form-group"}
               [:label {:for "inputFirstName" :class "col-lg-2 control-label"} "Nombre"]
               [:div {:class "col-lg-10"}
                [:input {:type "text" :class "form-control" :id "inputFirstName" :placeholder "" :name "firstname" :value first-name}]
                (when (contains? errors :first-name) [:div {:class "alert alert-danger"} "Debes ingresar tu nombre"])]]

              [:div {:class "form-group"}
               [:label {:for "inputLastName" :class "col-lg-2 control-label"} "Apellido"]
               [:div {:class "col-lg-10"}
                [:input {:type "text" :class "form-control" :id "inputLastName" :placeholder "" :name "lastname" :value last-name}]
                (when (contains? errors :last-name) [:div {:class "alert alert-danger"} "Debes ingresar tu apellido"])]]


              [:div {:class "form-group"}
               [:label {:for "inputEmail" :class "col-lg-2 control-label"} "Email"]
               [:div {:class "col-lg-10"}
                [:input {:type "text" :class "form-control" :id "inputEmail" :placeholder "Ej : mambo@gmail.com" :name "username" :value username}]
                (when (contains? errors :username) [:div {:class "alert alert-danger"} "Debes ingresar un email valido"])
                (when (contains? errors :username-exist) [:div {:class "alert alert-danger"} "Ya existe un usuario registrado con ese email"])]]

              [:div {:class "form-group"}
               [:label {:for "inputPassword" :class "col-lg-2 control-label"} "Contraseña"]
               [:div {:class "col-lg-10"}
                [:input {:type "password" :class "form-control" :id "inputPassword" :name "password"}]
                (when (contains? errors :password) [:div {:class "alert alert-danger"} "Debes ingresar una contraseña"])]]

              [:div {:class "form-group"}
               [:label {:for "inputPassword2" :class "col-lg-2 control-label"} " "]
               [:div {:class "col-lg-10"}
                [:input {:type "password" :class "form-control" :id "inputPassword2" :placeholder "Vuelve a escribir tu contraseña" :name "password2"}]]]

              [:div {:class "form-group"}
               [:label {:for "inputInvitation" :class "col-lg-2 control-label"} "Invitacion"]
               [:div {:class "col-md-10"}
                [:input {:type "text" :class "form-control" :id "invitation" :placeholder "" :name "invitation"}]
                (when (contains? errors :invitation) [:div {:class "alert alert-danger"} "Verifica tu nro de invitacion"])]]

              [:div {:class "form-group"}
               [:div {:class "col-md-offset-5 col-md-5 col-xs-offset-5 col-xs-5"}
                [:button {:type "submit" :class "btn btn-primary"} "Registrame"]]]]]]]]]]
       (gen/footer-includes)])))


