(ns zipper
  (:require
    [clojure.zip :as zip]
    ))

;; From https://clojure.org/reference/other_libraries#_zippers_functional_tree_editing_clojure_zip

(def data '[[a * b] + [c * d]])
(def dz (zip/vector-zip data))

;find the second *
#_(-> dz zip/down zip/right zip/right zip/down zip/right zip/remove zip/root)

;'remove' the first 2 terms
#_(-> dz zip/next zip/remove zip/node zip/remove zip/root)

#_(-> dz zip/next zip/next)

#_(loop [loc dz]
    (if (zip/end? loc)
      (zip/root loc)
      (recur
        (zip/next
          (if (= (zip/node loc) '*)
            ; (zip/replace loc '/)
            (do (println (zip/node loc))
                (zip/insert-right loc '[-]))
            loc)))))

