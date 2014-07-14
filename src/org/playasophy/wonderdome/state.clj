(ns org.playasophy.wonderdome.state
  (:require
    [clojure.core.async :as async :refer [<!]]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.mode.core :as mode]))


(defn initialize
  "Builds the initial system state map from the given configuration."
  [modes]
  {:mode/map modes
   :mode/playlist (keys modes)
   :mode/current (first (keys modes))})


(defn current-mode
  "Returns the current mode record from the state map."
  [state]
  (get (:mode/map state) (:mode/current state)))


(defn update-mode
  "Passes the input event to the current mode and returns the map with an
  updated mode state."
  [state event]
  (if (current-mode state)
    (update-in state [:mode/map (:mode/current state)] mode/update event)
    state))


(defrecord InputProcessor
  [handler input state-agent process]

  component/Lifecycle

  (start
    [this]
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
  [handler & {:keys [input state-agent]}]
  {:pre [(fn? handler)]}
  (InputProcessor. handler input state-agent nil))
