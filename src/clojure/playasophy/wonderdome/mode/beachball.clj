(ns playasophy.wonderdome.mode.beachball
  (:require
    [clojure.tools.logging :as log]
    [playasophy.wonderdome.geometry.sphere :as sphere]
    [playasophy.wonderdome.mode.core :as mode]
    [playasophy.wonderdome.util.color :as color]
    [playasophy.wonderdome.util.control :as control]))


(def speed-bounds [(/ sphere/tau 36) (* sphere/tau 3)])

(defrecord BeachballMode
  [theta speed color-lists index]

  mode/Mode

  (update
    [this event]
    (case [(:type event) (:input event)]
      [:time/tick nil]
      (let [elapsed (or (:elapsed event) 0.0)
            delta (* (/ elapsed 1000) speed)
            theta' (mod (+ theta delta) sphere/tau)]
        (assoc this :theta theta'))

      [:button/press :L]
      (assoc this :speed (first speed-bounds))

      [:button/press :R]
      (assoc this :speed (second speed-bounds))

      [:button/press :X]
      (assoc this :index (control/wrap [0 (count color-lists)] (inc index)))

      [:button/press :Y]
      (assoc this :index (control/wrap [0 (count color-lists)] (dec index)))

      [:axis/direction :y-axis]
      (assoc this :speed (control/adjust speed event
                                         :rate (/ sphere/tau 4)
                                         :min-val (first speed-bounds)
                                         :max-val (second speed-bounds)))

      this))


  (render
    [this pixel]
    (let [colors (nth color-lists index)
          color-arc (/ sphere/tau (count colors))
          pixel-theta (-> pixel :barrel :theta)
          theta' (+ pixel-theta (:theta this))
          theta' (mod theta' sphere/tau)
          color-index (int (/ theta' color-arc))]
      (nth colors color-index))))


(defn init
  "Creates a new beachball mode with starting speed and seq of colors."
  ([color-lists]
   (init (/ sphere/tau 4) color-lists))
  ([speed color-lists]
   (BeachballMode. 0 speed color-lists 0)))
