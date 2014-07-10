(ns org.playasophy.wonderdome.core
  (:require
    [com.stuartsierra.component :as component]))


#_
(def wonderdome-state
  "Example representation of the current wonderdome state."
  {:state :running
   :last-autocycle #inst "2014-07-07T11:03:42Z"
   :playlist [:colorcycle  ; queue is probably better
              :sweep
              :twinkle
              :marqee]
   :modes {:colorcycle {:speed 0.1
                        :density 2.6
                        :offset 0.3208}
           :sweep {:offset "..."}}
   ; instead of explicit handlers, use functional composition
   :event-handlers []})


; Core wonderdome system has an event loop which is running on a separate
; thread at some interval. Main 'update' mechanism gets a dt and sequence of
; events, should return an updated system.


(defn handler
  "Simple identity handler function."
  [system dt events]
  system)


; Autocycle handler:
; Sets :autocycle/at property, when receiving control events resets the timer
; to :autocycle/period seconds in the future. If current time passes the
; threshold, rotates to the next mode in the playlist.


; Konami code handler:
; Watches button inputs, keeping track of the last sequence of events; if the
; last event was too far in the past, clears the buffer. If the sequence matches
; the code, changes the current mode to the easter-egg mode. Probably lets the
; autocycle or manual mode change handle switching out of it.


; Mode handler:
; Passes the dt and events to the current mode via mode/update and returns the
; system with an updated mode state.


(defn initialize
  [{:keys [layout display modes]
    ; TODO: defaults?
    :as config}]
  (component/system-map
    :layout layout
    ; display is responsible for running its own rendering thread.
    :display (component/using display [:layout])
    ; input event queue?
    ; http server?
    #_
    (component/using
      (example-component config-options)
      {:database  :db
       :scheduler :scheduler})))
