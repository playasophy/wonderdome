(ns org.playasophy.wonderdome.state
  (:require
    [clojure.core.async :as async]
    [com.stuartsierra.component :as component]
    (org.playasophy.wonderdome
      [display :as display]
      [mode :as mode])))


; The current system state is wrapped in an agent, which the processor
; continuously sends an update function. The function pulls input off the
; queue, calls the handler to update the state, and resends to self.

; Configuration changes (such as pausing, changing the mode playlist, etc)
; can be accomplished by sending assoc's which alter the necessary
; configuration state.


(defn- current-mode
  "Determines the current mode from the system state map."
  [state]
  (get (:modes state) (:current-mode state)))


(defn- render!
  "Takes the current mode, maps it over the pixels in the layout, and calls
  set-colors! on the display. Modes should not have any mutable internal
  state."
  [mode layout display]
  (display/set-colors! display
    (map (partial map (partial mode/render mode))
         layout)))


(defn- process
  [state {:keys [handler input-channel layout display] :as context}]
  (if-not (:running state)
    state
    ; TODO: use alts!! to include a timeout here
    (let [event (async/<!! input-channel)
          state' (handler state event)]
      (render! (current-mode state') layout display)
      (send-off *agent* process context)
      state')))


(defrecord StateProcessor
  [state-agent handler input-channel layout display])

(extend-type StateProcessor
  component/Lifecycle

  (start
    [this]
    (send (:state-agent this) assoc :running true)
    (send-off (:state-agent this) process this)
    this)


  (stop
    [this]
    (send (:state-agent this) assoc :running false)
    this))


(defn processor
  [handler]
  (StateProcessor. nil handler nil nil nil))
