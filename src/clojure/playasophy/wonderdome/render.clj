(ns playasophy.wonderdome.render
  "The rendering component attaches a watch function to the state agent which
  forwards the new mode value onto the channel any time the current mode state
  changes. The process then maps the mode over the pixels in the layout to set
  the display colors."
  (:require
    [clojure.core.async :as async :refer [<! <!! >! >!!]]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [playasophy.wonderdome.mode.core :as mode]
    [playasophy.wonderdome.state :as state]
    [playasophy.wonderdome.display.core :as display]))


(defn- mode-watcher
  "Creates a ref watch function which will report any updates to the current
  mode to the given channel."
  [channel]
  (fn [_ _ old-state new-state]
    (let [old-mode (state/current-mode old-state)
          new-mode (state/current-mode new-state)]
      (when (and new-mode (not= old-mode new-mode))
        (>!! channel new-mode)))))


(defn- render!
  "Takes the given mode state, maps it over the pixels in the layout, and calls
  set-colors! on the display. The mode should not have any mutable internal
  state."
  [mode layout display]
  (try
    (display/set-colors! display
      (map (partial pmap (partial mode/render mode))
           layout))
    (catch Exception e
      (log/error e "Caught exception in render!"))))


(defrecord DisplayRenderer
  [mode-channel layout display state-agent process]

  component/Lifecycle

  (start
    [this]
    (add-watch state-agent :renderer (mode-watcher mode-channel))
    (if process
      (do
        (log/info "DisplayRenderer is already running")
        this)
      (do
        (log/info "Starting DisplayRenderer...")
        (assoc this :process
          (async/go-loop []
            (render! (<! mode-channel) layout display)
            (recur))))))


  (stop
    [this]
    (log/info "Stopping DisplayRenderer...")
    (remove-watch state-agent :renderer)
    (when process
      (async/close! process))
    (assoc this :process nil)))


(defn renderer
  "Creates a new display renderer which will pull mode values from a channel
  and render the layout to a display."
  [& {:keys [channel layout display state-agent]}]
  (DisplayRenderer. channel layout display state-agent nil))
