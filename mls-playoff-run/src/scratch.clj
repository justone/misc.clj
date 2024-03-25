(ns scratch
  (:require [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint print-table]]
            [clojure.string :as str]
            [org.httpkit.client :as http]))

(def api-key (System/getenv "API_FOOTBALL_API_KEY"))
(def base-url "https://v3.football.api-sports.io/")

;; Requests

(def base-req
  {:method :get
   :headers {"x-rapidapi-host" "v3.football.api-sports.io"
             "x-rapidapi-key" api-key}})

(def leagues-req
  (assoc base-req
         :url (str base-url "leagues")))


(defn rounds-req
  [league season]
  (assoc base-req
         :url (str base-url "fixtures/rounds")
         :query-params {:league league
                        :season season}))

(defn teams-req
  [league season]
  (assoc base-req
         :url (str base-url "teams")
         :query-params {:league league
                        :season season}))

; curl --request GET \
;      --url 'https://v3.football.api-sports.io/standings?league=39&season=2019' \
;      --header 'x-rapidapi-host: v3.football.api-sports.io' \
;      --header 'x-rapidapi-key: XxXxXxXxXxXxXxXxXxXxXxXx'

(defn standings-req
  [league season]
  (assoc base-req
         :url (str base-url "standings")
         :query-params {:league league
                        :season season}))

; curl --request GET \
;      --url 'https://v3.football.api-sports.io/fixtures?live=all' \
;      --header 'x-rapidapi-host: v3.football.api-sports.io' \
;      --header 'x-rapidapi-key: XxXxXxXxXxXxXxXxXxXxXxXx'

(defn fixtures-req
  [league season]
  (assoc base-req
         :url (str base-url "fixtures")
         :query-params {:league league
                        :season season}))

(defn status-req
  []
  (assoc base-req :url (str base-url "status")))

;; Caching

(def cache-base "cache")

(defn cache-key
  [req]
  (let [{:keys [url query-params]} req]
    (str/join
      "_"
      (cons (subs url (count base-url)) (map #(str (-> % key name) "-" (val %)) query-params)))))

#_(cache-key leagues-req)
#_(cache-key (teams-req 253 2022))

(defn- cache-file
  [cache-key]
  (io/file cache-base (format "%s.cache" cache-key)))

(defn cache-lookup
  [cache-key]
  (let [cache-file (cache-file cache-key)]
    (when (.exists cache-file)
      (-> cache-file
          slurp
          edn/read-string))))

(defn cache-save
  [resp cache-key]
  (let [cache-file (cache-file cache-key)]
    (.mkdirs (.getParentFile cache-file))
    (->> (with-out-str (pprint resp))
         (spit cache-file))))

(defn saveable?
  [response]
  (and (= 200 (:status response))
       (empty? (get-in response [:body :errors]))))

#_(cache-save {:body :thing} "foo")
#_(cache-lookup "foo")


;; Request fulfilment

(defn cached-request
  [req]
  (let [ckey (cache-key req)]
    (-> (or (let [response (cache-lookup ckey)]
              (when response
                (println (format "Loaded %s from cache" ckey)))
              response)
            (let [response (-> @(http/request req)
                               (update :body json/parse-string true))]
              (when (saveable? response)
                (println (format "Saved %s to cache" ckey))
                (cache-save response ckey))
              response))
        :body)))

(comment
  (-> leagues-req)

  (def leagues (cached-request leagues-req))
  (first (:response leagues))
  (def mls-teams (cached-request (teams-req 253 2024)))
  (def mls-standings (cached-request (standings-req 253 2024)))
  (def mls-fixtures (cached-request (fixtures-req 253 2024)))
  (def mls-rounds (cached-request (rounds-req 253 2024)))
  (saveable? (cache-lookup "bad_req"))
  (saveable? (cache-lookup (cache-key (teams-req 253 2022))))
  (print-table (sort-by :surface (map :venue (:response mls-teams))))
  (def status (cached-request (status-req))))

; (into [] (range 100))

(defn -main [& args]
  (println args))


(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))

