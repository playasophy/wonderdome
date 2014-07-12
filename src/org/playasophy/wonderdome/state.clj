(ns org.playasophy.wonderdome.state
  (:require
    [clojure.core.async :as async :refer [<!]]
    [com.stuartsierra.component :as component]))


(defn current-mode
  "Determines the current mode from the system state map."
  [state]
  (get (:modes state) (:current-mode state)))


(defrecord InputProcessor
  [handler input-channel state-agent process]

  component/Lifecycle

  (start
    [this]
    (when-not input-channel
      (throw (IllegalStateException.
               "InputProcessor can't be started without an input channel")))
    (when-not state-agent
      (throw (IllegalStateException.
               "InputProcessor can't be started without a state-agent")))
    (if process
      this
      (assoc this :process
        (async/go-loop []
          (send state-agent handler (<! input-channel))))))


  (stop
    [this]
    (when process
      (async/close! process))
    (assoc this :process nil)))


(defn input-processor
  [handler]
  {:pre [(fn? handler)]}
  (InputProcessor. handler nil nil nil))
