(ns playasophy.wonderdome.mode.ant
  (:require
    [playasophy.wonderdome.util.color :as color]
    [playasophy.wonderdome.util.control :as control])
  (:import
    playasophy.wonderdome.mode.Mode))


(def speed-bounds [1 10])
(def length-bounds [2 10])


(defrecord AntMode
  [position speed length hue saturation]

  Mode

  (update
    [this event]
    (condp = [(:type event) (:input event)]
      [:time/tick nil]
      (let [elapsed (or (:elapsed event) 0.0)
            hue' (+ (or hue 0.0) (* elapsed 0.00002))
            position' (+ position (* (/ elapsed 1000) speed))
            position' (if (>= position' 1200) 0 position')]
        (assoc this
          :position position'
          :hue hue'))

      [:button/press :L]
      (assoc this :speed (first speed-bounds) :length (second length-bounds))

      [:button/press :R]
      (assoc this :speed (second speed-bounds) :length (first length-bounds))

      [:button/press :A]
      (assoc this :length (control/bound length-bounds (inc length)))

      [:button/press :B]
      (assoc this :length (control/bound length-bounds (dec length)))

      [:button/press :X]
      (assoc this :speed (control/bound speed-bounds (inc speed)))

      [:button/press :Y]
      (assoc this :speed (control/bound speed-bounds (dec speed)))

      [:axis/direction :x-axis]
      (assoc this :hue (control/adjust hue event :rate 0.20 :min-val -1.0 :max-val 2.0))

      [:axis/direction :y-axis]
      (assoc this :saturation (control/adjust saturation event :rate 0.3))

      this))


  (render
    [this pixel]
    (if (< (rem (+ position (:pixel pixel)) (* 2 length)) length)
      (color/hsv hue saturation 1.0)
      color/none)))


(defn init
  "Creates a new ant mode with starting speed, and length."
  [speed length]
  (->AntMode 0 speed length 0.0 1.0))
