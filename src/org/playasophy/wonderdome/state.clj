(ns org.playasophy.wonderdome.state
  (:require
    [clojure.core.async :as async :refer [<!]]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.mode.core :as mode]))


;;;;; STATE FUNCTIONS ;;;;;

(defn current-mode
  "Returns the current mode record from the state map."
  [state]
  (get (:mode/map state) (:mode/current state)))


(defn next-mode
  "Returns the wonderdome state with the next mode in the playlist set as the
  current mode. Rotates the new mode to the back of the list."
  [state]
  (let [playlist (:mode/playlist state)
        next-mode (first playlist)
        playlist' (-> playlist rest vec (conj next-mode))]
    (log/debug (str "Switching mode to " next-mode " => updated playlist: " (pr-str playlist')))
    (assoc state
      :mode/current next-mode
      :mode/playlist playlist')))


(defn update-mode
  "Passes the input event to the current mode and returns the map with an
  updated mode state."
  [state event]
  (if (current-mode state)
    (update-in state [:mode/map (:mode/current state)] mode/update event)
    state))


(defn initialize
  "Builds the initial system state map from the given configuration."
  [modes playlist]
  (->
    {:mode/map modes
     :mode/playlist (or playlist (vec (keys modes)))}
    next-mode))



;;;;; EVENT PROCESSOR ;;;;;

(defrecord EventProcessor
  [handler input state-agent process]

  component/Lifecycle

  (start
    [this]
    (if process
      (do
        (log/info "EventProcessor is already running")
        this)
      (do
        (log/info "Starting EventProcessor...")
        (assoc this :process
          (async/go-loop []
            (send state-agent handler (<! input))
            (recur))))))


  (stop
    [this]
    (log/info "Stopping EventProcessor...")
    (when process
      (async/close! process))
    (assoc this :process nil)))


(defn processor
  [handler & {:keys [input state-agent]}]
  {:pre [(fn? handler)]}
  (EventProcessor. handler input state-agent nil))
