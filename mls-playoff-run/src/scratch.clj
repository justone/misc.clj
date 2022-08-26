(ns scratch
  (:require [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
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

#_(cache-save {:body :thing} "foo")
#_(cache-lookup "foo")


;; Request fulfilment

(defn cached-request
  [req]
  (let [ckey (cache-key req)]
    (-> (or (cache-lookup ckey)
            (doto (-> @(http/request req)
                      (update :body json/parse-string true))
              (cache-save ckey)))
        :body)))

#_(-> leagues-req)

#_(def leagues (cached-request leagues-req))
#_(first (:response leagues))
#_(def mls-teams (cached-request (teams-req 253 2022)))
#_(first (:response mls-teams))

; (into [] (range 100))

(defn -main [& args]
  (println args))


(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))

