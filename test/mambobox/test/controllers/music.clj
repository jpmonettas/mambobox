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
        result1 (search-music "" "" all-songs)
        result2 (search-music "ente negra" "" all-songs)
        result3 (search-music "sucurzal" "" all-songs)
        result4 (search-music "" "son" all-songs)
        result5 (search-music "" "" all-songs)]
    (is (= (count result1) 3) "All")
    (is (= (count result2) 1) "With query name")
    (is (= (count result3) 2) "With query artist")
    (is (= (count result4) 2) "With tag")
    (is (= (count result5) 3) "All with page")))
                   
        
(deftest test-zero-score-songs)

(deftest test-favourites-score-songs)

(deftest test-visits-score-songs)

(deftest test-make-scored-songs-col
  (let [songs [{:_id "521e4d5be4b030cc0edddd99" :name "1" :visits 10}
               {:_id "521d10e7e4b030cc0edddd94" :name "2" :visits 100}
               {:_id "521bcc75e4b030cc0edddd8c" :name "3" :visits 20}
               {:_id "521bcc05e4b030cc0edddd88" :name "4" :visits 5}
               {:_id "5226a0a4e4b023c2ee81714a" :name "5" :visits 2}]
        scored-songs1 (into [] (make-scored-songs-col songs nil nil))
        scored-songs2 (into [] (make-scored-songs-col songs ["5226a0a4e4b023c2ee81714a"] nil))
        scored-songs3 (into [] (make-scored-songs-col songs nil ["521bcc05e4b030cc0edddd88"]))
        scored-songs4 (into [] (make-scored-songs-col songs ["5226a0a4e4b023c2ee81714a"] ["521bcc05e4b030cc0edddd88" "521e4d5be4b030cc0edddd99" "5226a0a4e4b023c2ee81714a"]))]
    (is (every? identity (map #(:score %) scored-songs1)) "Check all have scores")
    (is (= (:score (get scored-songs1 0)) 10) "Check only visits")  
    (is (= (:score (get scored-songs1 1)) 100) "Check only visits 2")  
    (is (= (:score (get scored-songs2 4)) 0) "Zero score user fav")
    (is (= (:score (get scored-songs3 3)) 105) "Points per other user fav")
    (is (= (:score (get scored-songs4 0)) 110) "Combined")
    (is (= (:score (get scored-songs4 4)) 0) "Combined 2")))
