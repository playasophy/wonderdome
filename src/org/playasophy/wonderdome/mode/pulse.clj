(ns org.playasophy.wonderdome.mode.pulse
  (:require
    [org.playasophy.wonderdome.mode.core :as mode]
    [org.playasophy.wonderdome.util.color :as color]))

(defn generate-value
  [t alpha deg-per-ms]
    (+
     (*
      (/
       (Math/sin
        (* t deg-per-ms))
       4)
      alpha)
     0.75))


(defn generate-color
  [t alpha deg-per-ms]
  (let [v (generate-value t alpha deg-per-ms)
        h (mod (* t deg-per-ms) 360)]
    (color/hsv* h 1.0 v)))

(def ^:private ^:const adjustment-rate
  "Rate at which the deg-per-ms changes per-ms"
  0.000005)

(def ^:private ^:const max-rate
  ""
  (/ 1.0 1000))

(def ^:private ^:const min-rate
  ""
  0.0)

(defrecord PulseMode
  [colors alpha deg-per-ms accum-time]

  mode/Mode

  (update
   [this event]
    (case [(:type event) (:input event)]
      [:time/tick nil]
      (assoc this
        :accum-time
        (+ (:accum-time this) (:elapsed event))
        :colors
        (generate-color (:accum-time this) alpha deg-per-ms))

      [:axis/direction :x-axis]
      (let [delta (* (or (:value event) 0)
                     (or (:elapsed event) 0)
                     adjustment-rate)
            new-rate (-> deg-per-ms (+ delta) (min max-rate) (max min-rate))]
        (assoc this :deg-per-ms new-rate))

      this))

  (render
   [this pixel]
   (apply color/hsv colors)))

(defn pulse
  [color]
  "Creates a pulse mode starting at the given color/hsv"
  (PulseMode. color 1.0 (/ 6.0 100000.0) 0))
