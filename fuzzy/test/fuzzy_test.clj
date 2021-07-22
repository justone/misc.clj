(ns fuzzy-test
  (:require
    [fuzzy :refer [create-index fmatch]]
    [clojure.test :refer [deftest testing is]]))

(deftest fuzzy
  (testing "matching"
    (let [idx (create-index "a fuzzy brown fox")]
      (is (fmatch idx "zbf"))
      (is (fmatch idx "zzox"))
      (is (fmatch idx "zbf"))
      )))
