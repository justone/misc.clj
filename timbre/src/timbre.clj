(ns timbre
  (:require
    [clojure.tools.logging :as log]
    [clojure.string :as string]

    [taoensso.timbre :as timbre]
    [taoensso.timbre.tools.logging :as ttl]))

(defn test-fn
  []
  (log/debug "test debug level")
  (log/info "test info"))

#_(ttl/use-timbre)
#_(timbre/with-level :debug
    (test-fn))
#_(alter-var-root #'timbre/*config* #(assoc %1 :min-level :info))
#_(alter-var-root #'timbre/*config* assoc :min-level :info)
#_(alter-var-root #'timbre/*config* assoc :output-fn #(force (:msg_ %1)))
#_(timbre/set-level! :info)
#_(timbre/set-level! :debug)
taoensso.timbre.appenders.core/println-appender
; taoensso.timbre/default-output-fn
#_(timbre/println-appender :stream :std-err)
; timbre/spit-appender


(defn test-fn
  []
  (log/error "test ctl error level")
  (log/debug "test ctl debug level")
  (log/info "test ctl info")
  (timbre/error "test timbre error level")
  (timbre/debug "test timbre debug level")
  (timbre/info "test timbre info")
  )

(defn simple-output
  [data]
  ; (clojure.pprint/pprint data)
  (str
    (string/upper-case (name (:level data)))  " "
    (force (:msg_ data))))

#_(#'simple-output {:level :info :msg_ "foo"})


(defn -main
  [& args]
  (ttl/use-timbre)

  (timbre/swap-config! assoc :output-fn simple-output)
  (timbre/swap-config! assoc-in [:appenders :println] (timbre/println-appender {:stream :*err*}))
  (clojure.pprint/pprint timbre/*config*)
  #_(timbre/println-appender :stream :*err*)

  (println "before setting log level")
  (test-fn)

  (timbre/set-level! :info)

  (println "after setting log level to :info")
  (test-fn)
  )
