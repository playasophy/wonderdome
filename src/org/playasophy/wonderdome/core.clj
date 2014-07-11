(ns org.playasophy.wonderdome.core
  (:require
    [clojure.core.async :as async]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.input.timer :refer [timer]]))


(defn initialize
  [{:keys [layout display modes timer-ms handler]
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
    ; TODO: use a mix channel instead?
    :input-channel (async/chan 5)

    :timer
    (component/using
      (timer timer-ms)
      {:out :input-channel})

    ; Input sources run whatever threads are necessary and stick input events into
    ; the channel/queue for consumption by the system.
    ; TODO: http server
    ; TODO: usb input
    ; TODO: audio parser

    ; The current system state is wrapped in an agent. Configuration changes
    ; (such as pausing, changing the mode playlist, etc) can be accomplished by
    ; sending assoc's which alter the necessary configuration state.
    #_
    :state #_ (agent ...)

    #_
    :processor #_
    (component/using
      (processor handler)
      [:layout :display :state])

    ))
