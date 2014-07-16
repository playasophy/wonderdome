(ns org.playasophy.wonderdome.input.timer
  "Timer inputs periodically fire a :dt event after a certain number of
  milliseconds. The event will contain the number of milliseconds since the
  last event as an :elapsed value."
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
        (when-not (Thread/interrupted)
          (Thread/sleep period)
          (let [now (System/currentTimeMillis)]
            (>!! channel {:type :dt, :elapsed (- now t)})
            (recur now))))
      (catch InterruptedException e
        nil))))


(defrecord TimerInput
  [^long period channel ^Thread process]

  component/Lifecycle

  (start
    [this]
    (if process
      this
      (assoc this :process
        (doto (Thread. (timer-loop period channel) "TimerInput")
          (.setDaemon true)
          (.start)))))


  (stop
    [this]
    (when process
      (.interrupt process)
      (.join process 1000))
    (assoc this :process nil)))


(defn timer
  "Creates a new timer that will put an event on the output channel every period
  milliseconds."
  [channel period]
  {:pre [(some? channel) (pos? period)]}
  (TimerInput. period channel nil))
