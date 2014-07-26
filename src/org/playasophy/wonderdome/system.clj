(ns org.playasophy.wonderdome.system
  (:require
    [clojure.core.async :as async]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.render :as render]
    [org.playasophy.wonderdome.state :as state]
    [org.playasophy.wonderdome.web.app :as web]))


;;;;; CHANNEL MIXER ;;;;;

(defrecord ChannelMixer
  [inputs output mixer]

  component/Lifecycle

  (start
    [this]
    (log/info "Starting ChannelMixer...")
    (when-not output
      (throw (IllegalStateException.
               "ChannelMixer can't be started without an output channel")))
    (let [mix (or (:mixer this) (async/mix output))]
      (doall (map (partial async/admix mix) inputs))
      (assoc this :mixer mix)))


  (stop
    [this]
    (log/info "Stopping ChannelMixer...")
    (when mixer
      (dorun (map (partial async/unmix mixer) inputs)))
    (assoc this :mixer nil)))


(defn mixer
  "Creates a new component which will set up a channel mixer to put multiple
  input channels onto the given output channel."
  [& {:keys [inputs output]}]
  (ChannelMixer. (set inputs) output nil))



;;;;; SYSTEM INITIALIZATION ;;;;;

(defn add-input
  "Associates a new input source component into a system map. The input
  function should take an output channel as the first argument. If the function
  does not return an input, the channel will be closed."
  [system k input-fn channel & args]
  {:pre [(some? channel)]}
  (if-let [input (apply input-fn channel args)]
    (-> system
        (update-in [:mixer :inputs] conj channel)
        (assoc k input))
    (do
      (async/close! channel)
      system)))


(defn initialize
  [{:keys [layout display event-handler modes playlist web-options]}]
  (log/info "Initializing system components...")
  (component/system-map
    ; Input sources run whatever processes are necessary and stick input events
    ; into a channel which is mixed into a common event channel. Use 'add-input'
    ; to update the system with new input components.

    ; The input mixer manages independently-buffered channels from each input
    ; source and mixes them into the common event channel.
    :mixer
    (component/using
      (mixer)
      {:output :event-channel})

    :event-channel
    (async/chan 25)

    ; The event processor pulls events off the input channel and sends updates
    ; to the state agent via the event handler function.
    :processor
    (component/using
      (state/processor event-handler)
      {:input :event-channel
       :state-agent :state-agent})

    ; The current system state is wrapped in an agent. Configuration changes
    ; (such as pausing, changing the mode playlist, etc) can be accomplished by
    ; sending assoc's which alter the necessary configuration state.
    :state-agent
    (agent
      (state/initialize modes playlist)
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

    :layout layout
    :display display

    :web
    (web/server web-options)
    ))
