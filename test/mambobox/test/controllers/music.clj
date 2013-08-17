(ns mambobox.test.controllers.music
  (:use clojure.test
        mambobox.controllers.music))

(deftest test-is-youtube-link?
  (is (is-youtube-link? "http://www.youtube.com/watch?v=HkycF0hUtTQ"))
  (is (is-youtube-link? "youtube.com/watch?v=k_5YZbmLI4c"))
  (is (not (is-youtube-link? "youtube.com/watch?vk_5YZbmLI4c"))))
  
