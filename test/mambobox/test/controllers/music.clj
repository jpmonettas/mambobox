(ns mambobox.test.controllers.music
  (:use clojure.test
        mambobox.controllers.music))

(deftest test-is-youtube-link?
  (is (is-youtube-link? "http://www.youtube.com/watch?v=HkycF0hUtTQ"))
  (is (is-youtube-link? "youtube.com/watch?v=k_5YZbmLI4c"))
  (is (not (is-youtube-link? "youtube.com/watch?vk_5YZbmLI4c"))))
  
(deftest test-song-contains-tag?
  (let [song1 {:name "Song1"
               :tags ["son","mambo"]}]
    (is (song-contains-tag? song1 "mambo"))
    (is (not (song-contains-tag? song1 "rumba")))))


(deftest test-get-cant-pages
  (let [col1 [1 2 3 4 5 6]]
    (is (= (get-cant-pages col1 8) 1))
    (is (= (get-cant-pages col1 2) 3))
    (is (= (get-cant-pages col1 4) 2))))
        
(deftest test-get-collection-page
  (let [col1 [1 2 3 4 5 6 7 8]]
    (is (= (get-collection-page col1 1 3) [1 2 3]))
    (is (= (get-collection-page col1 2 3) [4 5 6]))
    (is (= (get-collection-page col1 3 3) [7 8]))
    
    (is (= (get-collection-page col1 1 10) [1 2 3 4 5 6 7 8]))
    (is (= (get-collection-page col1 3 2) [5 6]))))


(deftest test-music-search
  (let [all-songs [{:name "Vente negra"
                    :artist "Habana con kola"
                    :tags ["son", "rumba"]}
                   {:name "La sangre y la lluvia"
                    :artist "La sucursal"
                    :tags ["son"]}
                   {:name "No te puedo querer"
                    :artist "La sucursal"
                    :tags ["rumba"]}]
        result1 (:songs-found (search-music "user" "" "" 1 10 all-songs)) ;; All
        result2 (:songs-found (search-music "user" "ente negra" "" 1 10 all-songs)) ;; With query name
        result3 (:songs-found (search-music "user" "sucurzal" "" 1 10 all-songs)) ;; With query artist
        result4 (:songs-found (search-music "user" "" "son" 1 10 all-songs)) ;; With tag
        result5 (:songs-found (search-music "user" "" "" 1 2 all-songs))] ;; All with page
    (is (= (count result1) 3))
    (is (= (count result2) 1))
    (is (= (count result3) 2))
    (is (= (count result4) 2))
    (is (= (count result5) 2))))
                   
        

                   
(deftest test-make-suprise-me-vector
  (let [my-favs [1 2 3 4]
        all-favs [2 3 4 5 6 7 8]
        most-visited [1 3 4 15]]
    (is (= (make-surprise-me-vector my-favs all-favs  most-visited 2) [5 6 7 8]))
    (is (= (make-surprise-me-vector my-favs all-favs  most-visited 10) [5 6 7 8 15]))))
        
