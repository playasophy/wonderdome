(ns playasophy.wonderdome.mode.bombs
  (:require
    [clojure.tools.logging :as log]
    [playasophy.wonderdome.geometry.sphere :as sphere]
    [playasophy.wonderdome.util.color :as color]
    [playasophy.wonderdome.util.control :as control])
  (:import
    playasophy.wonderdome.mode.Mode))


(defn- distance
  [barrel-radius p q]
  (let [vertical-distance (Math/abs (- (:z p) (:z q)))
        angular-distance (Math/abs (- (:theta p) (:theta q)))
        angular-distance (if (> angular-distance sphere/pi) (- sphere/tau angular-distance) angular-distance)
        circle-distance (* barrel-radius angular-distance)]
    (Math/sqrt (+ (Math/pow vertical-distance 2) (Math/pow circle-distance 2)))))


(defn- overlay
  ([layers]
   (reduce (fn [c1 c2] (if (= c2 color/none) c1 c2)) layers)))


(defn- bomb-color
  [barrel-radius pixel bomb]
  (do
    #_(log/warn pixel bomb)
    (let [speed (:speed bomb)
          delta (distance barrel-radius (:center bomb) (:barrel pixel))
          radius (* (:age bomb) speed)]
      (if (<= delta radius)
        (color/rainbow (/ delta radius))
        color/none))))


(defrecord BombsMode
  [barrel-radius barrel-height cursor-position bombs]

  Mode

  (update
    [this event]
    (condp = [(:type event) (:input event)]
      [:time/tick nil]
      (let [elapsed (or (:elapsed event) 0.0)]
        (assoc this :bombs
          (mapv (fn [bomb]
                 (let [age' (+ (:age bomb) elapsed)]
                   (assoc bomb :age age')))
                (filter #(< (:age %) 10000) bombs))))

      [:axis/direction :y-axis]
      (assoc-in this [:cursor-position :z]
             (control/adjust (:z cursor-position) event
                             :rate 0.24
                             :min-val 0.0
                             :max-val barrel-height))

      [:axis/direction :x-axis]
      (assoc-in this [:cursor-position :theta]
        (let [theta' (control/adjust-wrapped
                       (:theta cursor-position) event
                       :rate sphere/pi
                       :min-val 0.0
                       :max-val sphere/tau)]
          (if (>= theta' sphere/tau) 0.0 theta')))

      [:button/press :A]
      (assoc-in this [:bombs]
        (conj bombs {:center cursor-position :age 0 :speed 0.00001}))

      [:button/press :B]
      (assoc-in this [:bombs]
        (conj bombs {:center cursor-position :age 0 :speed 0.00003}))

      [:button/press :X]
      (assoc-in this [:bombs]
        (conj bombs {:center cursor-position :age 0 :speed 0.00006}))

      [:button/press :Y]
      (assoc-in this [:bombs]
        (conj bombs {:center cursor-position :age 0 :speed 0.00012}))

      this))


  (render
    [this pixel]
    (do
      #_(log/warn pixel bombs)
      (let [bomb-colors (mapv (partial bomb-color barrel-radius pixel) bombs)
            cursor-delta (distance barrel-radius (:barrel pixel) cursor-position)
            cursor-color (if (< cursor-delta 0.04) (color/gray 1.0) color/none)]
        (overlay (conj bomb-colors cursor-color))))))


(defn init
  "Creates a new bombs mode with starting parameters"
  ([]
   (init 0.099 (* 0.068 8) {:theta 0 :z 0.20}))
  ([barrel-radius barrel-height cursor-position]
   (->BombsMode barrel-radius barrel-height cursor-position [])))
