(ns playasophy.wonderdome.input.audio-harness
  (:require
    [clojure.core.async :as async :refer [<!]]
    [com.stuartsierra.component :as component]
    [playasophy.wonderdome.input.audio :as audio]
    [playasophy.wonderdome.util.color :as color]
    [quil.core :as quil]))


(def ^:constant ^:private history-size 100)


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
    (let [beat-elapsed (- (System/currentTimeMillis) (:beat/at audio))
          saturation (/ (Math/pow 1.5 (/ beat-elapsed 100.0)))]
      (quil/stroke 0)
      (quil/fill (color/rgb saturation 0 0))
      (quil/rect 0 0 (quil/width) 20)
      (quil/fill (color/gray 0.8))
      (quil/text (format "%4.1f" (:beat/power audio)) 20 15))
    (let [spectrums (:freq/spectrum audio)
          slice-width (/ (quil/width) history-size)]
      (dotimes [t history-size]
        (let [index (mod (+ t (:freq/pos audio)) history-size)
              bands (nth spectrums index)
              band-height (/ (- (quil/height) 20) (count bands))
              x-pos (* index slice-width)]
          (dotimes [f (count bands)]
            (let [power (nth bands f)
                  exposure (- 1.0 (Math/exp (- (/ power 12))))
                  y-pos (+ 20 (* (- (count bands) f) band-height))
                  ;color (color/rainbow (* 0.8 (- 1.0 exposure)))
                  color (color/gray exposure)]
              (quil/fill color)
              (quil/stroke color)
              (quil/rect x-pos y-pos slice-width band-height)))))
      (when-let [current (nth spectrums (:freq/pos audio))]
        (dotimes [f (count current)]
          (let [band-height (/ (- (quil/height) 20) (count current))
                y-pos (+ 20 (* (dec (- (count current) f)) band-height))]
            (quil/fill (color/rgb 0 1 0.5))
            (quil/text (format "%2d: %4.1f" f (nth current f)) 0 (+ 15 y-pos))))))))


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
                           :freq/spectrum (vec (repeat history-size [0.0]))
                           :freq/pos 0})]
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
                  (swap! state
                    (fn [s]
                      (-> s
                          (assoc :freq/at now)
                          (update-in [:freq/spectrum] assoc (or (:freq/pos s) 0) (:spectrum event))
                          (update-in [:freq/pos] #(mod (inc (or % 0)) history-size)))))))
              (recur))

            :sketch
            (quil/sketch
              :title "Wonderdome Audio Harness"
              :features [:resizable]
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
