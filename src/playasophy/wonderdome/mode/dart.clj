(ns playasophy.wonderdome.mode.dart
  (:require
    [clojure.tools.logging :as log]
    [playasophy.wonderdome.geometry.sphere :as sphere]
    [playasophy.wonderdome.mode.core :as mode]
    [playasophy.wonderdome.util.color :as color]))


(def ^:private ^:const move-rate
  "Rate at which the angles change in radians per millisecond."
  0.25)


(defrecord DartMode
  [offset scale]

  mode/Mode

  (update
    [this event]
    (case (:type event)
      :time/tick
      (let [elapsed (or (:elapsed event) 0.0)
            offset' (+ offset (* (/ move-rate 1000) elapsed))
            offset' (if (> offset' 4.0) (- offset' 4.0) offset')]
        (assoc this :offset offset'))

      ; default
      this))


  (render
    [this pixel]
    (let [[_ polar] (:sphere pixel)]
      (if (< (* 2 polar) offset)
        (color/rainbow (+ polar offset))
        0))))


(defn dart
  "Creates a new dart mode."
  []
  (DartMode. 1.0 1.0))
