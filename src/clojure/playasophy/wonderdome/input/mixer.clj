(ns playasophy.wonderdome.input.mixer
  (:require
    [clojure.core.async :as async]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]))


(defrecord ChannelMixer
  [inputs output mixer]

  component/Lifecycle

  (start
    [this]
    (log/info "Starting ChannelMixer...")
    (let [mix (or mixer (async/mix output))]
      (dorun (map (partial async/admix mix) inputs))
      (assoc this :mixer mix)))


  (stop
    [this]
    (log/info "Stopping ChannelMixer...")
    (when mixer
      (dorun (map (partial async/unmix mixer) inputs)))
    (assoc this :mixer nil)))


(defn channel-mixer
  "Creates a new component which will set up a channel mixer to put multiple
  input channels onto the given output channel."
  [& {:keys [inputs output]}]
  (ChannelMixer. (set inputs) output nil))
