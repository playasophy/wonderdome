(ns org.playasophy.wonderdome.core
  (:require
    [clojure.core.async :as async]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.mode :as mode]))


(defn identity-handler
  "Simple identity handler function."
  [state input]
  state)


; Autocycle handler:
; Sets :autocycle/at property, when receiving control inputs resets the timer
; to :autocycle/period seconds in the future. If current time passes the
; threshold, rotates to the next mode in the playlist.


; Konami code handler:
; Watches button inputs, keeping track of the sequence of input events; if the
; last input was too far in the past, clears the buffer. If the sequence matches
; the code, changes the current mode to the easter-egg mode. Probably lets the
; autocycle or manual mode change handle switching out of it.


; Mode handler:
; Passes the dt and inputs to the current mode via mode/update and returns the
; system with an updated mode state.


#_
(defn render
  "Takes the current mode, maps it over the pixels in the layout, and calls
  set-colors! on the display. Modes should not have any mutable internal
  state."
  [mode layout display]
  (set-colors! display
    (map (partial map (partial mode/render mode))
         layout)))


(defn initialize
  [{:keys [layout display modes]
    :defaults {:timer-ms 30}
    :as config}]
  (component/system-map
    ; Layout is a pre-computed collection of strips of pixel coordinates.
    :layout layout

    ; Display is responsible for running its own rendering thread if necessary.
    ; Processing does, pixel-pusher doesn't.
    :display display

    ; Ideally use separate channels merged together, because we're probably only
    ; ever interested in the last 1-2 audio frames, but never want to lose
    ; button presses.
    #_ :input-channel #_ (async/chan 5)

    ; Input sources run whatever threads are necessary and stick input events into
    ; the channel/queue for consumption by the system.
    ; TODO: http server
    ; TODO: usb input
    ; TODO: audio parser
    ; TODO: timer?

    ; Handler functions recieve the current state of the system, a time delta,
    ; and a collection of input events, and return the updated system state.
    #_
    :handler

    ; The current system state is wrapped in an agent, which continuously sends
    ; itself an update function. The update pulls input off the queue, calls the
    ; handler to update the state, and recurs with a new last-updated time.
    ; Configuration changes (such as pausing, changing the mode playlist, etc)
    ; can be accomplished by sending assoc's which alter the necessary
    ; configuration state.
    #_
    :state
    ))
