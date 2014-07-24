(ns org.playasophy.wonderdome.display.pixel-pusher
  (:require
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.display.core :as display])
  (:import
    (com.heroicrobot.dropbit.devices.pixelpusher
      Strip)
    (com.heroicrobot.dropbit.registry
      DeviceRegistry)))


;;;;; CONFIGURATION ;;;;;

(defn- registry-observer
  "Constructs a new callback function which will update the strips in the given
  atom when the device registry changes."
  [^DeviceRegistry registry strips]
  (reify java.util.Observer
    (update [this target device]
      (let [new-strips (vec (.getStrips registry))]
        (println "Updated device:" device (str "(" (count new-strips) " strips)"))
        (reset! strips new-strips)))))


(defn- set-pixels!
  "Renders a sequence of pixel colors to an LED strip."
  [^Strip strip colors]
  (dorun (map #(when %2 (.setPixel strip (int %1) (unchecked-int %2)))
              (range (.getLength strip))
              colors)))



;;;;; PIXEL PUSHER DISPLAY ;;;;;

(defrecord PixelPusherDisplay
  [^DeviceRegistry registry strips running]

  component/Lifecycle

  (start
    [this]
    (println "Starting PixelPusher display...")
    (when-not running
      (doto registry
        (.addObserver (registry-observer registry strips))
        (.startPushing)
        (.setExtraDelay 0)
        (.setAutoThrottle true)))
    (assoc this :running true))


  (stop
    [this]
    (println "Stopping PixelPusher display...")
    ; TODO: remove device observer
    (.stopPushing registry)
    (reset! strips [])
    (assoc this :running false))


  display/Display

  (set-colors!
    [this colors]
    (when running
      (dorun (map set-pixels! @strips colors)))
    nil))


(defn pixel-pusher
  "Creates a new display streaming color commands to connected pixel-pusher
  hardware."
  [registry]
  (PixelPusherDisplay. registry (atom []) false))
