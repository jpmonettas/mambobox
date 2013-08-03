(ns mambobox.controllers.music
  (:use [mambobox.views.music-search :only [music-search-view]]
        [mambobox.views.music-detail :only [music-detail-view]]))

(def result-col-example
  (list 
   {:song-name "Vente Negra",
    :artist "Habana con Kola",
    :tags ["son" "guaguanco"],
    :num-of-comments 6}
   {:song-name "Esperanza",
    :artist "Salsa Celtica",
    :tags ["chacha"],
    :num-of-comments 4}))

(defn music-search []
  (let [result-col-example (list 
                            {:id 1
                             :song-name "Vente Negra",
                             :artist "Habana con Kola",
                             :tags ["son" "guaguanco"],
                             :num-of-comments 6}
                            {:id 2
                             :song-name "Esperanza",
                             :artist "Salsa Celtica",
                             :tags ["chacha"],
                             :num-of-comments 4})]
        (music-search-view result-col-example)))
  
(defn music-id [id]
  (let [song {:id 1
              :song-name "Vente Negra",
              :artist "Habana con Kola",
              :tags ["son" "guaguanco"],
              :num-of-comments 6}]
  (music-detail-view song)))

(defn edit-music [id song-name artist])