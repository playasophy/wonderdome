(ns playasophy.wonderdome.mode.bombs
  (:require
    [clojure.tools.logging :as log]
    [playasophy.wonderdome.geometry.sphere :as sphere]
    [playasophy.wonderdome.mode.core :as mode]
    [playasophy.wonderdome.util.color :as color]
    [playasophy.wonderdome.util.control :as control]))


(defn- distance
  [barrel-radius p q]
  (let [vertical-distance (Math/abs (- (:z p) (:z q)))
        angular-distance (Math/abs (- (:theta p) (:theta q)))
        angular-distance (if (> angular-distance sphere/pi) (- sphere/tau angular-distance) angular-distance)
        circle-distance (* barrel-radius angular-distance)]
    (Math/sqrt (+ (Math/pow vertical-distance 2) (Math/pow circle-distance 2)))))


(defrecord BombsMode
  [barrel-radius barrel-height cursor-position]

  mode/Mode

  (update
    [this event]
    (condp = [(:type event) (:input event)]
      [:button/press :L]
      this

      [:axis/direction :y-axis]
      (assoc-in this [:cursor-position :z]
             (control/adjust (:z cursor-position) event
                             :rate 0.24
                             :min-val 0.0
                             :max-val barrel-height))

      [:axis/direction :x-axis]
      (assoc-in
        this [:cursor-position :theta]
        (let [theta' (control/adjust-wrapped
                       (:theta cursor-position) event
                       :rate sphere/pi
                       :min-val 0.0
                       :max-val sphere/tau)]
          (if (>= theta' sphere/tau) 0.0 theta')))

      this))


  (render
    [this pixel]
    (let [delta (distance barrel-radius (:barrel pixel) cursor-position)]
      (if (< delta 0.04) (color/gray 1.0) color/none))))


(defn init
  "Creates a new bombs mode with starting parameters"
  ([]
   (init 0.099 (* 0.068 8) {:theta 0 :z 0.20}))
  ([barrel-radius barrel-height cursor-position]
   (BombsMode. barrel-radius barrel-height cursor-position)))
