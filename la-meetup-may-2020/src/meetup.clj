(ns meetup
  (:require
    [clojure.test.check.generators :as gen]))

(def vowels #{\a \e \i \o \u \A \E \I \O \U})

(defn vowel-indices
  [input]
  (filter some? (map-indexed #(when (vowels %2) %1) input)))

#_(vowel-indices "abcde")
#_(vowel-indices "aeiou")

(defn shortest-distance
  [indices idx]
  (->> (map #(- idx %) indices)
       (map #(Math/abs %))
       (apply min)))

#_(shortest-distance [0 4] 1)
#_(shortest-distance [0 4] 0)
#_(shortest-distance [0 4] 2)

(defn nearest-vowels
  [input]
  (let [indices (vowel-indices input)]
    (map (partial shortest-distance indices) (range (count input)))))

#_(nearest-vowels "aeiou")
#_(nearest-vowels "babbb")
#_(nearest-vowels "abcdefghijklmnopqrstuvwxyz")


;; Live coded solution, create during the meetup

(defn track-nearest [pred xs]
  (first (reduce (fn [[acc last-seen i] x]
            (if (pred x)
              [(conj acc 0) i (inc i)]
              [(conj acc (when last-seen (- i last-seen))) last-seen (inc i)]))
          [[] nil 0]
          xs)))

#_(track-nearest (set "aeiou") "babbbb")
#_(reverse (track-nearest (set "aeiou") (reverse "babbbb")))

(defn nearest-vowels-exp
  [input]
  (map (fnil min Integer/MAX_VALUE Integer/MAX_VALUE)
       (track-nearest (set "aeiou") input)
       (reverse (track-nearest (set "aeiou") (reverse input)))))

#_(nearest-vowels-exp "aeiou")
#_(let [string (gen/generate (gen/vector gen/char-alpha) 1000000)]
    (time (nearest-vowels-exp string))
    nil)
