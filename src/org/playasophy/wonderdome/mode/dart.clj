(ns org.playasophy.wonderdome.mode.dart
  (:require
    [org.playasophy.wonderdome.geometry.sphere :as sphere]
    [clojure.tools.logging :as log]
    [org.playasophy.wonderdome.mode.core :as mode]
    [org.playasophy.wonderdome.util.color :as color]))


(def ^:private ^:const move-rate
  "Rate at which the angles change in radians per millisecond."
  0.25)


(defrecord DartMode
  [offset scale]

  mode/Mode

  (update
    [this event]
    (case [(:type event)]
      [:time/tick]
      (let [elapsed (or (:elapsed event) 0.0)
            offset' (+ offset (* (/ move-rate 1000) elapsed))
            offset' (if (> offset' 4.0) (- offset' 4.0) offset')]
        (assoc this :offset offset'))

      ; default
      this))


  (render
    [this pixel]
    (let [angle (sphere/angle-offset (:sphere pixel) [0 0 0])
          polar (nth (:sphere pixel) 2)
          index (* scale (- angle offset))
          white (if (< (+ polar angle) offset) (+ polar offset) 0.0)]
      (color/rainbow white))))

(defn dart
  "Creates a new dart mode."
  []
  (DartMode. 1.0 1.0))
