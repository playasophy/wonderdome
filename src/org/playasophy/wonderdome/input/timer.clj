(ns org.playasophy.wonderdome.input.timer
  (:require
    [clojure.core.async :as async :refer [>!!]]
    [com.stuartsierra.component :as component]))


(defn- timer-loop
  "Constructs a new runnable looping function which puts an event on the given
  output channel every period milliseconds. The loop can be terminated by
  interrupting the thread."
  ^Runnable
  [period channel]
  (fn []
    (try
      (loop [t (System/currentTimeMillis)]
        (Thread/sleep period)
        (let [now (System/currentTimeMillis)]
          (>!! channel {:type :dt, :elapsed (- now t)})
          (recur now)))
      (catch InterruptedException e
        nil))))


(defrecord TimerInput
  [^long period ^Thread thread output]

  component/Lifecycle

  (start
    [this]
    (when-not output
      (throw (IllegalStateException. "Cannot start TimerInput without output channel")))
    ; TODO: create sliding-buffer and mix into output?
    (if thread
      this
      (assoc this :thread
        (doto (Thread. (timer-loop period output) "TimerInput")
          (.setDaemon true)
          (.start)))))


  (stop
    [this]
    (when thread
      (.interrupt thread)
      (.join thread 1000))
    (assoc this :thread nil)))


(defn timer
  "Creates a new timer that will put an event on the output channel every period
  milliseconds."
  [period]
  {:pre [(pos? period)]}
  (TimerInput. period nil nil))
