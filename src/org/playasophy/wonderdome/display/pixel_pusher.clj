(ns org.playasophy.wonderdome.display.pixel-pusher
  (:require
    [clojure.tools.logging :as log]
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
        (log/info (str "Updated device: " device))
        (reset! strips new-strips)))))


(defn- set-pixels!
  "Renders a sequence of pixel colors to an LED strip."
  [^Strip strip colors]
  (dorun (map #(when %2 (.setPixel strip (unchecked-int %2) (int %1)))
              (range (.getLength strip))
              colors)))



;;;;; PIXEL PUSHER DISPLAY ;;;;;

(defrecord PixelPusherDisplay
  [^DeviceRegistry registry strips running]

  component/Lifecycle

  (start
    [this]
    (log/info "Starting PixelPusher display...")
    (.startPushing registry)
    (assoc this :running true))


  (stop
    [this]
    (log/info "Stopping PixelPusher display...")
    (.stopPushing registry)
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
  []
  (let [registry (DeviceRegistry.)
        strips (atom [])]
    (doto registry
      (.addObserver (registry-observer registry strips))
      (.setExtraDelay 0)
      (.setAutoThrottle true))
    (PixelPusherDisplay. registry strips false)))
