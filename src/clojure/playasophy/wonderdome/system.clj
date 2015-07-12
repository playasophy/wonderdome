(ns playasophy.wonderdome.system
  (:require
    [clojure.core.async :as async :refer [>!!]]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [playasophy.wonderdome.render :as render]
    [playasophy.wonderdome.state :as state]
    [playasophy.wonderdome.input.mixer :as mixer]
    [playasophy.wonderdome.web.app :as web]))


(defn initialize
  [config]
  (log/info "Initializing system components...")
  (when-not config
    (throw (IllegalArgumentException.
             "Cannot initialize system without config map")))
  (merge
    (component/system-map
      ; The input mixer manages independently-buffered channels from each input
      ; source and mixes them into the common event channel.
      :mixer
      (component/using
        (mixer/channel-mixer)
        {:inputs :input-channels
         :output :event-channel})

      :event-channel
      (async/chan 25)

      ; The event processor pulls events off the input channel and sends updates
      ; to the state agent via the event handler function.
      :processor
      (component/using
        (state/processor (:event-handler config state/update-mode))
        {:input :event-channel
         :state-agent :state-agent})

      ; The current system state is wrapped in an agent. Configuration changes
      ; (such as pausing, changing the mode playlist, etc) can be accomplished by
      ; sending assoc's which alter the necessary configuration state.
      :state-agent
      (agent
        (state/initialize (:modes config) (:playlist config))
        :error-handler
        (fn [a ex]
          (log/error ex "Error updating agent state!")))

      :mode-channel
      (async/chan (async/sliding-buffer 3))

      ; The rendering process watches for changes to the current mode value and
      ; renders new modes over the layout to set the display colors.
      :renderer
      (component/using
        (render/renderer)
        [:mode-channel :state-agent :layout :display])

      :web
      (component/using
        (web/server (:web-options config))
        [:event-channel :state-agent]))
    (select-keys config [:input-channels :layout :display])
    (:inputs config)))


(defn render-current
  "Forcibly renders the current mode state."
  [{:keys [mode-channel state-agent]}]
  (when-let [current (state/current-mode @state-agent)]
    (>!! mode-channel current)))
