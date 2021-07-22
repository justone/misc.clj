(ns twitter
  (:require
    [twttr.api :as api]
    [twttr.auth :refer [->UserCredentials]]
    [clojure.java.browse :as browse]))

#_(def creds (->UserCredentials
               "xxxxxxxxxxxxxxxxxxxxxxxxx"
               "yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy"
               "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"
               "ppppppppppppppppppppppppppppppppppppppppppppp"))

(defn print-meta
  [data]
  (clojure.pprint/pprint (meta data))
  data)


#_(->> (api/statuses-user-timeline creds :params {:screen_name "ndj" :count 5})
       print-meta
       (map :text)
       )

#_(->> (api/followers-list creds :screen_name "ndj" :count 5)
       print-meta
       )

#_(browse/browse-url "https://google.com/")
#_(deref browse/*open-url-script*)
#_(reset! browse/*open-url-script* :uninitialized)

#_(for [x (range 4)
        y (range 4)
        :when (< y 3)]
    [x y])

#_(for [x (range 4)
        y (range 4)]
    x)

#_(for [match [{:id 1 :games [1 2 3]}
               {:id 2 :games [4 5 6]}]
        game (:games match)
        :when (< game 4)]
    match)

#_(mapcat :games [{:id 1 :games [1 2 3]}
                  {:id 2 :games [4 5 6]}])

#_(map #(hash-map :id %) [1 2 3])
#_(for [id [1 2 3]] {:id id})

(for [x [1 2 nil 3] :when x]
  x)

(let [coll (range 5)]
  [(split-with #(< % 2) (range 5))
   (partition-by #(< % 2) (range 5))]
  )

(split-with #(< % 2) [1 4 3 0 2])
(partition-by #(< % 2) [1 4 3 0 2])

#_(split-with #(< % 2) (shuffle (range 5)))
#_(partition-by #(< % 2) '(3 0 1 4 2))

#_(println (apply str (repeat 1000 "conjure\n")))

#_(* 60 60)

#_(let [_ 4]
    (* _ _))

