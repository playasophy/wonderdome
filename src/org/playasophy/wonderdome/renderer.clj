(ns org.playasophy.wonderdome.renderer
  (:require
    [clojure.core.async :as async :refer [<! <!! >! >!!]]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.mode :as mode]
    [org.playasophy.wonderdome.state :as state]
    [org.playasophy.wonderdome.display.core :as display]))


(defn- mode-watcher
  "Creates a ref watch function which will report any updates to the current
  mode to the given channel."
  [channel]
  (fn [_ _ old-state new-state]
    (let [old-mode (state/current-mode old-state)
          new-mode (state/current-mode new-state)]
      (when (and new-mode (not= old-mode new-mode))
        (>!! channel new-mode)))))


; TODO: move to display.core?
(defn- render!
  "Takes the given mode state, maps it over the pixels in the layout, and calls
  set-colors! on the display. The mode should not have any mutable internal
  state."
  [mode layout display]
  (display/set-colors! display
    (map (partial map (partial mode/render mode))
         layout)))


(defrecord DisplayRenderer
  [channel layout display state-agent process]

  component/Lifecycle

  (start
    [this]
    (when-not state-agent
      (throw (IllegalStateException.
               "DisplayRenderer can't be started without a state-agent")))
    (when-not layout
      (throw (IllegalStateException.
               "DisplayRenderer can't be started without a layout")))
    (when-not display
      (throw (IllegalStateException.
               "DisplayRenderer can't be started without a display")))
    (add-watch state-agent :renderer (mode-watcher channel))
    (if process
      this
      (assoc this :process
        (async/go-loop []
          (render! (<! channel) layout display)
          (recur)))))


  (stop
    [this]
    (when state-agent
      (remove-watch state-agent :renderer))
    (when process
      (async/close! process))
    (assoc this :process nil)))


(defn renderer
  "Creates a new display renderer which will pull mode states from the given
  channel."
  [channel]
  {:pre [(some? channel)]}
  (DisplayRenderer. channel nil nil nil nil))
