(ns meetup)

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
