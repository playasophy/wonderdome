(ns org.playasophy.wonderdome.display.pixel-pusher
  (:require
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.display :as display])
  (:import
    ;[com.heroicrobot.dropbit.registry *]
    com.heroicrobot.dropbit.devices.pixelpusher.Strip))


;;;;; WONDERDOME DISPLAY ;;;;;

(defrecord PixelPusherDisplay
  [registry strips running])

(extend-type PixelPusherDisplay
  component/Lifecycle

  (start
    [this]
    (println "Starting PixelPusher display...")
    ; TODO: add device observer
    (assoc this :running true))


  (stop
    [this]
    (println "Stopping PixelPusher display...")
    ; TODO: clear strips, remove device observer?
    (assoc this :running false))


  display/Display

  (set-pixels!
    [this pixels]
    ; TODO: implementation
    this))


(defn pixel-pusher-display
  "Creates a new display streaming color commands to connected pixel-pusher
  hardware."
  ([registry]
   (PixelPusherDisplay. registry (atom []) false)))
