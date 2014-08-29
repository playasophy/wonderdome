(ns org.playasophy.wonderdome.mode.ant
  (:require
    [org.playasophy.wonderdome.mode.core :as mode]
    [org.playasophy.wonderdome.util.color :as color]
    [org.playasophy.wonderdome.util.control :as control]))


(defrecord AntMode
  [position speed length hue saturation]

  mode/Mode

  (update
    [this event]
    (condp = [(:type event) (:button event)]
      [:time/tick nil]
      (let [elapsed (or (:elapsed event) 0.0)
            hue' (+ (or hue 0.0) (* elapsed 0.00002))
            position' (+ position (* (/ elapsed 1000) speed))
            position' (if (>= position' 1200) 0 position')]
        (assoc this
          :position position'
          :hue hue'))

      [:button/press :L]
      (assoc this :speed 1)

      [:button/press :R]
      (assoc this :speed 10)

      [:button/press :A]
      (assoc this :length 2)

      [:button/press :B]
      (assoc this :length 10)

      [:button/repeat :x-axis]
      (assoc this :hue (control/adjust hue event :rate 0.20 :min-val -1.0 :max-val 2.0))

      [:button/repeat :y-axis]
      (assoc this :saturation (control/adjust saturation event :rate 0.3))

      this))


  (render
    [this pixel]
    (if (< (rem (+ position (:pixel pixel)) (* 2 length)) length)
      (color/hsv hue saturation 1.0)
      color/none)))


(defn ant
  "Creates a new ant mode with starting speed, and length."
  [speed length]
  (AntMode. 0 speed length 0.0 1.0))
