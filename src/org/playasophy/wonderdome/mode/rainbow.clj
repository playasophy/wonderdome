(ns org.playasophy.wonderdome.mode.rainbow
  (:require
    [org.playasophy.wonderdome.mode.core :as mode]
    [org.playasophy.wonderdome.util.color :as color]
    [org.playasophy.wonderdome.util.control :as control]))


(defrecord RainbowMode
  [scale speed offset]

  mode/Mode

  (update
    [this event]
    (case [(:type event) (:input event)]
      [:time/tick nil]
      (let [elapsed (or (:elapsed event) 0.0)
            offset' (+ offset (* (/ speed 1000) elapsed))
            offset' (if (> offset' 1.0) (- offset' 1.0) offset')]
        (assoc this :offset offset'))

      [:axis/direction :x-axis]
      (assoc this :scale
             (control/adjust scale event
                             :rate 0.5
                             :min-val 0.1
                             :max-val 5.0))

      [:axis/direction :y-axis]
      (assoc this :speed
             (control/adjust speed event
                             :rate 0.5
                             :min-val -3.0
                             :max-val  3.0))

      ; default
      this))


  (render
    [this pixel]
    (let [[_ angle _] (:sphere pixel)
          index (- (* scale angle) offset)]
      (color/rainbow index))))


(defn rainbow
  "Creates a new rainbow color-cycling mode."
  []
  (RainbowMode. 1.0 1.0 0.0))
