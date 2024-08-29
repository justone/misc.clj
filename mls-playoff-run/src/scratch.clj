(ns scratch
  (:require [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint print-table]]
            [clojure.string :as str]
            [clojure.string :as string]
            [org.httpkit.client :as http])
  (:import (java.time ZonedDateTime ZoneId)
           (java.time.format DateTimeFormatter)))

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
  (filter (comp #(= % "Leagues Cup") :name :league) (:response leagues))
  (filter #(string/includes? % "Cup") (map (comp :name :league) (:response leagues)))
  (def mls-teams (cached-request (teams-req 253 2024)))
  (def mls-standings (cached-request (standings-req 253 2024)))
  (def mls-fixtures (cached-request (fixtures-req 253 2024)))
  (def open-cup-fixtures (cached-request (fixtures-req 257 2024)))
  (def leagues-cup-fixtures (cached-request (fixtures-req 772 2024)))
  (def mls-fixtures (cached-request (fixtures-req 253 2024)))
  (def mls-rounds (cached-request (rounds-req 253 2024)))
  (saveable? (cache-lookup "bad_req"))
  (saveable? (cache-lookup (cache-key (teams-req 253 2022))))
  (print-table (sort-by :surface (map :venue (:response mls-teams))))
  (def status (cached-request (status-req))))

(defn fixture-played-by?
  [teams]
  (fn [fixture]
    (or (teams (-> fixture :teams :home :name))
        (teams (-> fixture :teams :away :name)))))

(defn fixture-status?
  [status]
  (fn [fixture]
    (= status (-> fixture :fixture :status :short))))

(defn fixture-round?
  [round]
  (fn [fixture]
    (= round (-> fixture :league :round))))

(defn fixture->opponent
  [fixture for-whom]
  (let [home-team (-> fixture :teams :home :name)
        home-goals (-> fixture :goals :home)
        away-team (-> fixture :teams :away :name)
        away-goals (-> fixture :goals :away)
        ]
    (if (= home-team for-whom)
      (str "vs " away-team " (" (or home-goals "x") "-" (or away-goals "x") ")")
      (str "at " home-team " (" (or away-goals "x") "-" (or home-goals "x") ")"))))

(defn team-schedules
  [rounds fixtures teams]
  (for [round rounds
        :let [round-fixture (first (filter (fixture-round? round) fixtures))]
        ]
    (reduce
      (fn [result team]
        (let [fixtures (->> fixtures
                            (filter (fixture-played-by? #{team}))
                            (filter (fixture-round? round))
                            )
              fixture (first fixtures)]
          (assoc result team (some-> fixture (fixture->opponent team)))))
      {:round round
       :date (-> round-fixture :fixture :date #_#_(string/split #"T") first)}
      teams)))

(defn team-schedules2
  [fixtures teams]
  (->> (for [team teams
             fixture (->> fixtures
                          (filter (fixture-played-by? #{team}))
                          #_(filter (fixture-status? "NS")))
             :let [dt-str (-> fixture :fixture :date)
                   dt (-> (ZonedDateTime/parse dt-str DateTimeFormatter/ISO_OFFSET_DATE_TIME)
                          (.withZoneSameInstant (ZoneId/systemDefault)))]]
         {:date (.format dt DateTimeFormatter/ISO_LOCAL_DATE)
          team (some-> fixture (fixture->opponent team))})
       (group-by :date)
       vals
       (map #(apply merge %))))


(comment
  (fixture->opponent (last (:response mls-fixtures)) "Seattle Sounders")
  (map (comp :name :team) (:response mls-teams))
  (->> (group-by (comp :round :league) (:response mls-fixtures))
       (into {} (map (juxt first (comp count second)))))
  (->> (:response mls-fixtures)
       (filter (fixture-played-by? #{"Los Angeles Galaxy"}))
       (filter (fixture-status? "FT"))
       last)
  (tap> (drop 35 (:response mls-rounds)))
  (->> (team-schedules (drop 35 (:response mls-rounds))
                       (:response mls-fixtures)
                       ["Los Angeles Galaxy" "Los Angeles FC"])
       (print-table [:round :date "Los Angeles Galaxy" "Los Angeles FC"]))
  (let [teams ["Los Angeles Galaxy" "Los Angeles FC" "Inter Miami" #_"FC Cincinnati"]]
    (->> (team-schedules2 (:response leagues-cup-fixtures) teams)
         (sort-by :date)
         (print-table (cons :date teams))))
  (let [teams ["Los Angeles FC" "Sporting Kansas City"]]
    (->> (team-schedules2 (:response open-cup-fixtures) teams)
         (sort-by :date)
         (print-table (cons :date teams))))
  (-> (ZonedDateTime/parse "2024-09-01T02:30:00+00:00" DateTimeFormatter/ISO_OFFSET_DATE_TIME)
      (.withZoneSameInstant (ZoneId/systemDefault)))
  )

; (into [] (range 100))

(defn -main [& args]
  (println args))


(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))

