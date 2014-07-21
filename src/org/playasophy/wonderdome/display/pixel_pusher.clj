(ns org.playasophy.wonderdome.display.pixel-pusher
  (:require
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.display.core :as display])
  (:import
    #_
    (com.heroicrobot.dropbit.devices.pixelpusher
      Strip)
    #_
    (com.heroicrobot.dropbit.registry
      DeviceRegistry)))


;;;;; CONFIGURATION ;;;;;

; Suppress spurious log output.
#_
(->
  DeviceRegistry
  .getName
  java.util.logging.Logger/getLogger
  (.setLevel java.util.logger.Level/WARNING))


(defn- registry-observer
  "Constructs a new callback function which will update the strips in the given
  atom when the device registry changes."
  [registry strips]
  (reify java.util.Observer
    (update [this target device]
      (let [new-strips (vec (.getStrips registry))]
        (println "Updated device:" device (str "(" (count new-strips) " strips)"))
        (reset! strips new-strips)))))



;;;;; PIXEL PUSHER DISPLAY ;;;;;

(defrecord PixelPusherDisplay
  [registry strips running]

  component/Lifecycle

  (start
    [this]
    (println "Starting PixelPusher display...")
    (doto registry
      (.addObserver (registry-observer registry strips))
      (.startPushing)
      (.setExtraDelay 0)
      (.setAutoThrottle true))
    (assoc this :running true))


  (stop
    [this]
    (println "Stopping PixelPusher display...")
    ; TODO: clear strips, remove device observer?
    (.stopPushing registry)
    (assoc this :running false))


  display/Display

  (set-colors!
    [this colors]
    ; TODO: implementation
    nil))


(defn pixel-pusher
  "Creates a new display streaming color commands to connected pixel-pusher
  hardware."
  [registry]
  (PixelPusherDisplay. registry (atom []) false))
