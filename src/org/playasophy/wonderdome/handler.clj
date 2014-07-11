(ns org.playasophy.wonderdome.handler)

; Handler functions recieve the current state of the system and an input event
; and return the updated system state.


(defn identity-handler
  "Simple identity handler function."
  [state event]
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
