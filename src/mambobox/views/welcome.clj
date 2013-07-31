(ns mambobox.views.welcome
  (:require [mambobox.views.common :as common]
            [noir.content.getting-started])
  (:use [noir.core :only [defpage]]))

(defpage "/welcome" []
         (common/layout
           [:p "Welcome to mambobox"]))
