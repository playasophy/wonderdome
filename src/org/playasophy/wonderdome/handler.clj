(ns org.playasophy.wonderdome.handler
  (:require
    [org.playasophy.wonderdome.mode :as mode]))


;;;;; EVENT HANDLERS ;;;;;

; Handler functions recieve the current state of the system and an input event
; and return the updated system state.

(defn identity-handler
  "Simple identity handler function."
  [state event]
  state)


; TODO: move to state ns?
(defn mode-handler
  "Passes the input event to the current mode and returns the map with an
  updated mode state."
  [state event]
  (update-in state [:modes (:current-mode state)] mode/update event))



;;;;; HANDLER MIDDLEWARE ;;;;;

; Middleware wraps a handler function to produce a new handler with some extra
; logic. This is very similar to Ring middlewares.

(defn print-events
  "Debugging middleware which prints out events as they pass through the
  handler stack. A predicate function may be provided to filter the events
  shown."
  ([handler]
   (print-events handler (constantly true)))
  ([handler pred]
   (fn [state event]
     (when (pred event)
       (prn event))
     (handler state event))))


; Autocycle middleware:
; Sets :autocycle/at property, when receiving control inputs resets the timer
; to :autocycle/period seconds in the future. If current time passes the
; threshold, rotates to the next mode in the playlist.


; Konami code middleware:
; Watches button inputs, keeping track of the sequence of input events; if the
; last input was too far in the past, clears the buffer. If the sequence matches
; the code, changes the current mode to the easter-egg mode. Probably lets the
; autocycle or manual mode change handle switching out of it.
