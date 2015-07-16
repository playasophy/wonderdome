(ns playasophy.wonderdome.mode.bombs
  (:require
    [clojure.tools.logging :as log]
    [playasophy.wonderdome.geometry.sphere :as sphere]
    [playasophy.wonderdome.mode.core :as mode]
    [playasophy.wonderdome.util.color :as color]))


(defn- distance
  [barrel-radius p q]
  (let [vertical-distance (Math/abs (- (:z p) (:z q)))
        angular-distance (Math/abs (- (:theta p) (:theta q)))
        angular-distance (if (> angular-distance sphere/pi) (- sphere/tau angular-distance) angular-distance)
        circle-distance (* barrel-radius angular-distance)]
    (Math/sqrt (+ (Math/pow vertical-distance 2) (Math/pow circle-distance 2)))))


(defrecord BombsMode
  [barrel-radius cursor-position]

  mode/Mode

  (update
    [this event]
    (condp = [(:type event) (:input event)]
      [:button/press :L]
      this

      this))


  (render
    [this pixel]
    (let [delta (distance barrel-radius (:barrel pixel) cursor-position)]
      (if (< delta 0.05) (color/gray 1.0) color/none))))


(defn init
  "Creates a new bombs mode with starting parameters"
  ([]
   (init 0.099 {:theta 0 :z 0.15}))
  ([barrel-radius cursor-position]
   (BombsMode. barrel-radius cursor-position)))
