(ns fuzzy)

(defn create-index
  [string]
  (reduce #(assoc %1 %2 %1) {} (reverse string)))

(defn fmatch
  [idx string]
  (some? (get-in idx string)))

(defn create-index-regex
  [input]
  (let [restr (apply str (conj (into [] (interleave (repeat ".*") input)) ".*"))]
    (re-pattern restr))
  )

(defn fmatch-regex
  [idx string]
  (re-matches idx string))


#_(fmatch (create-index "abcdabcd") "ba")
#_(fmatch-regex (create-index-regex "ab") "abcdabcd")

;; Helping out Dorab

#_(let [iter (.iterator (.keySet (java.lang.System/getProperties)))]
    (for [i (iterator-seq iter)]
      i))

#_(into []
        (for [r (iterator-seq resp)
              gar (iterator-seq (.getResultsList r))]
          (.getAdGroupCriterion gar)))

#_(mapv (fn [r] (mapv (fn [gar] (.getAdGroupCriterion gar)) (.getResultsList r))) resp)

#_(map #(or %1 %2) [nil] [3])

;; FizzBuzz

#_(let [pattern [nil nil "Fizz"
                 nil "Buzz" "Fizz"
                 nil nil "Fizz"
                 "Buzz" nil "Fizz"
                 nil nil "FizzBuzz"]]
    (map #(or %1 %2) (cycle pattern) (range 1 101)))
