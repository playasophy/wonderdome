(ns org.playasophy.wonderdome.input.audio-harness
  (:require
    [clojure.core.async :as async :refer [<!]]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.input.audio :as audio]
    [org.playasophy.wonderdome.util.color :as color]
    [quil.core :as quil]))


(def system nil)


(defn- setup
  []
  (quil/frame-rate 30)
  (quil/text-font (quil/create-font "Courier" 18 true))
  (quil/background 0))


(defn- render
  []
  (quil/background 0)
  (when-let [audio (some-> system :state deref)]
    (let [spectrum (:freq/spectrum audio)
          band-height (/ (- (quil/height) 40) (inc (count spectrum)))]
      (doseq [i (range (count spectrum))]
        (quil/with-translation [20 (+ 20 (* i band-height))]
          (let [power (nth spectrum i)]
            (quil/fill (color/rgb 0 0 1))
            (quil/rect 20 5 (* power 10) (- band-height 10))
            (quil/fill (color/gray 0.8))
            (quil/text (str i) 0 5) ; TODO: frequency?
            (quil/text (format "%4.1f" power) 25 5)))))
    (let [beat-elapsed (- (System/currentTimeMillis) (:beat/at audio))
          decay-r 0.2
          saturation (/ (Math/pow (+ 1.0 decay-r) (/ beat-elapsed 100.0)))]
      (quil/with-translation [(- (quil/width) 30) (- (quil/height) 30)]
        (quil/fill (color/rgb saturation 0 0))
        (quil/ellipse 0 0 20 20)
        (quil/fill (color/gray 0.8))
        (quil/text (format "%4.1f" (:beat/power audio)) -30 5)))))


(defn init!
  "Initializes the audio harness system."
  []
  (when-not system
    (alter-var-root #'system
      (constantly
        (let [channel (async/chan (async/dropping-buffer 10))
              state (atom {:beat/at 0
                           :beat/power 0.0
                           :freq/at 0
                           :freq/spectrum []})]
          (component/system-map
            :state state
            :input (audio/audio-input channel 100)
            :channel channel
            :process
            (async/go-loop []
              (let [event (<! channel)
                    now (System/currentTimeMillis)]
                (case (:type event)
                  :audio/beat
                  (swap! state assoc :beat/at now :beat/power (:power event))
                  :audio/freq
                  (swap! state assoc :freq/at now :freq/spectrum (:spectrum event))))
              (recur))

            :sketch
            (quil/sketch
              :title "Wonderdome Audio Harness"
              :setup #'setup
              :draw #'render
              :size [750 500])))))))


(defn start!
  "Creates and starts a sketch to demonstrate the audio input functionality."
  []
  (when-not system
    (init!))
  (alter-var-root #'system component/start)
  :start)


(defn stop!
  "Stops the audio harness."
  []
  (when system
    (component/stop system)
    (alter-var-root #'system (constantly nil)))
  :stop)
