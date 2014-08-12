(ns org.playasophy.wonderdome.handler
  "Middleware functions for providing system capabilities by handling input
  events.

  Handler functions recieve the current state of the system and an input event
  and return the updated system state. Middleware wraps a handler function to
  produce a new handler with some extra logic. This is very similar to how Ring
  middleware functions."
  (:require
    [clojure.tools.logging :as log]
    [org.playasophy.wonderdome.state :as state])
  (:import
    java.util.Date))


(defn log-events
  "Debugging middleware which logs events as they pass through the handler
  stack. A predicate function may be provided to filter the events shown."
  ([handler]
   (log-events handler (constantly true)))
  ([handler pred]
   (fn [state event]
     (when (pred event)
       (log/debug (pr-str event)))
     (handler state event))))


(defn system-reset
  "Resets the system by exiting the JVM when the start button is held down."
  [handler & {:keys [threshold]
              :or {threshold 5000}}]
  (let [button-state (atom [false 0])
        inc-held (fn [[pressed? elapsed] dt]
                   (if pressed?
                     [true (+ elapsed dt)]
                     [false 0]))]
    (fn [state event]
      ; Update button state.
      (case [(:type event) (:button event)]
        [:time/tick nil]
        (swap! button-state inc-held (:elapsed event 0))

        [:button/press :start]
        (reset! button-state [true 0])

        [:button/release :start]
        (reset! button-state [false 0])

        nil)
      ; Check for reset condition.
      (let [[pressed? elapsed] @button-state]
        (when (and pressed? (> elapsed threshold))
          (log/warn "Resetting system because start button held for " elapsed " ms!")
          (System/exit 0)))
      ; Otherwise, pass on to handler chain.
      (handler state event))))


(defn mode-selector
  "Uses :select button presses to change the current mode."
  [handler]
  (fn [state event]
    (if (and (= (:type event) :button/press)
             (= (:button event) :select))
      (state/next-mode state)
      (handler state event))))


(defn autocycle-modes
  "Adds properties to the event state and automatically switches the current
  mode if no input matching a predicate has been received in a certain amount
  of time."
  [handler input? & {:keys [period enabled]
                     :or {period 300
                          enabled true}}]
  (fn [state event]
    (let [period (or (:autocycle/period state) period)
          now (System/currentTimeMillis)
          target (Date. (long (+ now (* 1000 period))))]
      (cond-> state
        ; Set enabled default if not found in state map.
        (nil? (find state :autocycle/enabled))
        (assoc :autocycle/enabled enabled)

        ; Set default period if not found.
        (nil? (:autocycle/period state))
        (assoc :autocycle/period period)

        ; If no target time is set, add it.
        (nil? (:autocycle/at state))
        (assoc :autocycle/at target)

        ; Input events push autocycle target back.
        (input? event)
        (assoc :autocycle/at target)

        ; If target cycle time is passed, rotate modes.
        (and (:autocycle/enabled state)
             (:autocycle/at state)
             (> now (.getTime ^Date (:autocycle/at state))))
        (-> (assoc :autocycle/at target)
            state/next-mode)

        ; Forward events to handler chain.
        true
        (handler event)))))


; Konami code middleware:
; Watches button inputs, keeping track of the sequence of input events; if the
; last input was too far in the past, clears the buffer. If the sequence matches
; the code, changes the current mode to the easter-egg mode. Probably lets the
; autocycle or manual mode change handle switching out of it.
