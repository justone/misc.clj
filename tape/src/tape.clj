(ns tape
  (:require
    [qbits.tape.tailer :as tailer]
    [qbits.tape.appender :as appender]
    [qbits.tape.queue :as queue]
    ))

;; create a queue instance
#_(def q (queue/make "/tmp/q2"))

;; create a tailer bound to that queue
#_(def t (tailer/make q))

;; nothing in queue yet, so nil
#_(tailer/read! t)

;; to add to queue you need an appender
#_(def appender (appender/make q))

;; add stuff to queue, returns index
#_(appender/write! appender {:foo [:bar {:baz 0}]})
#_(appender/write! appender {:another :thing})

#_(run! #(appender/write! appender %) (range 100))

#_(drop 10 t)

(defn add
  []
  (+))


#_(tailer/read! t)
#_(tailer/read! t)
#_(tailer/read! t)
#_(take 30 t)


;; back to some existing index, essentially rewinding to it
#_(tailer/to-index! t 79585743994881)
#_(tailer/to-start! t)

;; Tailers are also Sequential/Seqable/Reducible and behave as such.

#_(run! (fn [msg] (println msg)) t)

#_(doseq [msg t]
    (println msg))

#_(mod (* 60 60 1000))
#_[(quot (System/currentTimeMillis) (* 60 60 1000))
   (float (/ (System/currentTimeMillis) (* 60 60 1000)))]

#_(clojure.set/map-invert {:foo 1 :bar 1})

#_(defmulti tester second)
(defmulti tester #(second %&))
#_(def tester nil)

(defmethod tester :foo
  [arg1 arg2]
  arg2)

(defmethod tester :foo2
  [arg1 arg2 arg3]
  arg2)

#_(tester :bar :foo)
#_(tester :bar :foo2 :baz)

; isa?
; (ancestors String)
; (derive String ::mine)
; (isa? String ::yours)
; (make-hierarchy)
; clojure.core/global-heirarchy

#_(* 60 60 24 7 2)
#_(type 'true)
#_(= 'true (symbol "true"))
#_(= true 'true)
