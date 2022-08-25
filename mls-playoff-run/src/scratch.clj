(ns scratch
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
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


(defn teams-req
  [league season]
  (assoc base-req
         :url (str base-url "teams")
         :query-params {:league league
                        :season season}))

;; Caching

(defn cache-key
  [req]
  (let [{:keys [url query-params]} req]
    (str/join
      "_"
      (cons (subs url (count base-url)) (map #(str (-> % key name) "-" (val %)) query-params)))))

#_(cache-key leagues-req)
#_(cache-key (teams-req 253 2022))

(defn cache-lookup
  [req]
  (let [cache-key (cache-key req)]
    (some-> (io/file cache-key))))

(defn cache-save
  [resp req])


;; Request fulfilment

(defn request
  [req]
  (-> (or (cache-lookup req)
          (doto @(http/request req)
            (cache-save req)))
      :body
      (json/parse-string true)))

#_(-> leagues-req)

#_(def leagues (request leagues-req))
#_(first (:response leagues))
#_(def mls-teams (request (teams-req 253 2022)))
#_(first (:response mls-teams))

; (into [] (range 100))

(defn -main [& args]
  (println args))


(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
