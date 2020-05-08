(ns meetup-test
  (:require
    [clojure.test :refer :all]
    [meetup :refer :all]
    ))

(deftest main
  (testing "aeiou"
    (is (= [0 0 0 0 0] (nearest-vowels "aeiou"))))
  (testing "babbb"
    (is (= [1 0 1 2 3] (nearest-vowels "babbb"))))
  (testing "babbba"
    (is (= [1 0 1 2 1 0] (nearest-vowels "babbba"))))
  (testing "babba"
    (is (= [1 0 1 1 0] (nearest-vowels "babba"))))
  (testing "bAbabA"
    (is (= [1 0 1 0 1 0] (nearest-vowels "bAbabA"))))
  (testing "shopper"
    (is (= [2 1 0 1 1 0 1] (nearest-vowels "shopper"))))
  )
