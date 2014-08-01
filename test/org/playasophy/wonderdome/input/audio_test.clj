(ns org.playasophy.wonderdome.input.audio-test
  (:require
    [clojure.core.async :as async :refer [<!]]
    [clojure.test :refer :all]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.input.audio :as audio]
    [org.playasophy.wonderdome.util.color :as color]
    [quil.core :as quil]))


(def audio-state
  (atom {:beat/at 0
         :beat/power 0.0
         :freq/at 0
         :freq/spectrum []}))


(defn- setup-harness
  []
  (quil/frame-rate 30)
  (quil/text-font (quil/create-font "Courier" 18 true))
  (quil/background 0))


(defn- render-harness
  []
  (quil/background 0)
  (let [audio @audio-state]
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


(defn audio-harness
  "Creates and starts a sketch to demonstrate the audio input functionality."
  []
  (let [channel (async/chan (async/dropping-buffer 10))]
    {:channel channel

     :input
     (component/start (audio/audio-input channel 100))

     :process
     (async/go-loop []
       (let [event (<! channel)
             now (System/currentTimeMillis)]
         (case (:type event)
           :audio/beat
           (swap! audio-state assoc :beat/at now :beat/power (:power event))
           :audio/freq
           (swap! audio-state assoc :freq/at now :freq/spectrum (:spectrum event))))
       (recur))

     :sketch
     (quil/sketch
       :title "Wonderdome Audio Harness"
       :setup setup-harness
       :draw #'render-harness
       :size [750 500])}))
