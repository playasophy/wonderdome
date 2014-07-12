(ns org.playasophy.wonderdome.state
  (:require
    [clojure.core.async :as async]
    [com.stuartsierra.component :as component]))


; The current system state is wrapped in an agent, which the processor
; continuously sends an update function. The function pulls input off the
; queue and sends it to the agent handler 

; Configuration changes (such as pausing, changing the mode playlist, etc)
; can be accomplished by sending assoc's which alter the necessary
; configuration state.


(defn current-mode
  "Determines the current mode from the system state map."
  [state]
  (get (:modes state) (:current-mode state)))



(defrecord StateProcessor
  [state-agent handler input-channel layout display])

(extend-type StateProcessor
  component/Lifecycle

  (start
    [this]
    ; ...
    this)


  (stop
    [this]
    ; ...
    this))


(defn processor
  [handler]
  (StateProcessor. nil handler nil nil nil))
