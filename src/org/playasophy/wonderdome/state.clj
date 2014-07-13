(ns org.playasophy.wonderdome.state
  (:require
    [clojure.core.async :as async :refer [<!]]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.mode.core :as mode]))


(defn current-mode
  "Determines the current mode from the system state map."
  [state]
  (get (:modes state) (:current-mode state)))


(defn update-mode
  "Passes the input event to the current mode and returns the map with an
  updated mode state."
  [state event]
  (if (current-mode state)
    (update-in state [:modes (:current-mode state)] mode/update event)
    state))


(defrecord InputProcessor
  [handler input state-agent process]

  component/Lifecycle

  (start
    [this]
    (when-not input
      (throw (IllegalStateException.
               "InputProcessor can't be started without an input channel")))
    (when-not state-agent
      (throw (IllegalStateException.
               "InputProcessor can't be started without a state-agent")))
    (if process
      this
      (assoc this :process
        (async/go-loop []
          (send state-agent handler (<! input))
          (recur)))))


  (stop
    [this]
    (when process
      (async/close! process))
    (assoc this :process nil)))


(defn input-processor
  [handler]
  {:pre [(fn? handler)]}
  (InputProcessor. handler nil nil nil))
