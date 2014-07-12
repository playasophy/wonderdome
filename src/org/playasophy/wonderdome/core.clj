(ns org.playasophy.wonderdome.core
  (:require
    [clojure.core.async :as async]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.renderer :refer [renderer]]
    [org.playasophy.wonderdome.state :refer [input-processor]]
    [org.playasophy.wonderdome.input.timer :refer [timer]]))


(defn initialize
  [{:keys [layout display modes timer-ms handler initial-state]
    :or {timer-ms 30
         handler identity
         initial-state {}}}]
  (component/system-map
    ; Layout is a pre-computed collection of strips of pixel coordinates.
    :layout layout

    ; Display is responsible for running its own rendering thread if necessary.
    ; Processing does, pixel-pusher doesn't.
    :display display

    ; Ideally use separate channels mixed together, because we're probably only
    ; ever interested in the last 1-2 audio frames, but never want to lose
    ; button presses.
    ; TODO: use a mix channel instead?
    :input-channel (async/chan 5)

    :timer-input
    (component/using
      (timer timer-ms)
      {:output :input-channel})

    ; Input sources run whatever threads are necessary and stick input events into
    ; the channel/queue for consumption by the system.
    ; TODO: http server
    ; TODO: usb input
    ; TODO: audio parser

    ; The input processor pulls events off the input channel and sends updates
    ; to the state agent.
    :processor
    (component/using
      (input-processor handler)
      [:input-channel :state-agent])

    ; The current system state is wrapped in an agent. Configuration changes
    ; (such as pausing, changing the mode playlist, etc) can be accomplished by
    ; sending assoc's which alter the necessary configuration state.
    :state-agent (agent initial-state)

    :renderer
    (component/using
      (renderer (async/chan (async/sliding-buffer 5)))
      [:state-agent :layout :display])
    ))
