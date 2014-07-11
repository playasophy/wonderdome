(ns org.playasophy.wonderdome.input.timer
  (:require
    [clojure.core.async :as async]
    [com.stuartsierra.component :as component]))


(defn- timer-loop
  "Constructs a new runnable looping function which puts an event on the given
  output channel every period milliseconds."
  [period channel]
  (fn []
    (try
      (loop [t (System/currentTimeMillis)]
        (Thread/sleep period)
        (let [now (System/currentTimeMillis)]
          (async/>!! channel {:type :dt, :elapsed (- now t)})
          (recur now)))
      (catch InterruptedException e
        nil))))


(defrecord TimerInput
  [period out thread])

(extend-type TimerInput
  component/Lifecycle

  (start
    [this]
    (println (str "Starting " this "..."))
    (when-not (:out this)
      (throw (IllegalStateException. "Cannot start TimerInput without output channel")))
    (if (:thread this)
      (do
        (println (str this " is already running"))
        this)
      (let [run (timer-loop (:period this) (:out this))]
        (assoc this :thread
          (doto (Thread. run "TimerInput")
            (.setDaemon true)
            (.start))))))


  (stop
    [this]
    (println (str "Stopping " this "..."))
    (when-let [^Thread thread (:thread this)]
      (.interrupt thread)
      (.join thread 1000))
    (assoc this :thread nil)))


(defn timer
  "Creates a new timer that will put an event on the output channel every period
  milliseconds."
  ([period]
   (TimerInput. period nil nil))
  ([period out]
   (TimerInput. period out nil)))
