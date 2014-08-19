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

(defrecord PulseMode
  [colors alpha deg-per-ms accum-time]

  mode/Mode

  (update
   [this event]
    (if (= :time/tick (:type event))
      (assoc this 
        :accum-time
        (+ (:accum-time this) (:elapsed event))
        :colors
        (generate-color (:accum-time this) alpha deg-per-ms))
      this)
    )

  (render
   [this pixel]
   (apply color/hsv colors)))

(defn pulse
  [color]
  "Creates a pulse mode starting at the given color/hsv"
  (PulseMode. color 1.0 (/ 6.0 100000) 0))
