(ns org.playasophy.wonderdome.mode.rainbow
  (:require
    [org.playasophy.wonderdome.geometry.sphere :as sphere]
    [org.playasophy.wonderdome.mode.core :as mode]
    [org.playasophy.wonderdome.util.color :as color]))


(def ^:private ^:const move-rate
  "Rate at which the angles change in radians per millisecond."
  0.0005)


(defrecord RainbowMode
  [polar azimuth scale speed offset]

  mode/Mode

  (update
    [this event]
    (condp = [(:type event) (:button event)]
      [:dt nil]
      (let [elapsed (or (:elapsed event) 0.0)
            offset' (+ offset (* (/ speed 1000) elapsed))
            offset' (if (> offset' 1.0) (- offset' 1.0) offset')]
        (assoc this :offset offset'))

      ; TODO: add controls for adjusting speed and scale

      [:button/repeat :x-axis]
      (let [delta (* (or (:value event) 0)
                     (or (:elapsed event) 0)
                     move-rate)]
        (assoc this :azimuth
          (sphere/wrap-angle (+ azimuth delta))))

      [:button/repeat :y-axis]
      (let [delta (* (or (:value event) 0)
                     (or (:elapsed event) 0)
                     move-rate)]
        (assoc this :polar
          (sphere/wrap-angle (+ polar delta))))))


  (render
    [this pixel]
    (let [[_ pixel-polar pixel-azimuth] (:sphere pixel)
          ; TODO: calculate angular distance between polar/azimuth target:
          ; http://math.stackexchange.com/questions/231221/great-arc-distance-between-two-points-on-a-unit-sphere
          index (* scale (- pixel-polar offset))]
      (color/rainbow index))))


(defn rainbow
  "Creates a new rainbow color-cycling mode."
  []
  (RainbowMode. 0.0 0.0 1.0 1.0 0.0))
