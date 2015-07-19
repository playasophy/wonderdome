(ns playasophy.wonderdome.mode.beachball
  (:require
    [clojure.tools.logging :as log]
    [playasophy.wonderdome.geometry.sphere :as sphere]
    [playasophy.wonderdome.mode.core :as mode]
    [playasophy.wonderdome.util.color :as color]
    [playasophy.wonderdome.util.control :as control]))


(defrecord BeachballMode
  [theta speed colors]

  mode/Mode

  (update
    [this event]
    (condp = [(:type event) (:input event)]
      [:time/tick nil]
      (let [elapsed (or (:elapsed event) 0.0)
            delta (* (/ elapsed 1000) speed)
            theta' (mod (+ theta delta) sphere/tau)]
        (assoc this :theta theta'))

#_(      [:button/press :L]
      (assoc this :speed 1)

      [:button/press :R]
      (assoc this :speed 10)

      [:button/press :A]
      (assoc this :length 2)

      [:button/press :B]
      (assoc this :length 10)

      [:axis/direction :x-axis]
      (assoc this :hue (control/adjust hue event :rate 0.20 :min-val -1.0 :max-val 2.0))

      [:axis/direction :y-axis]
      (assoc this :saturation (control/adjust saturation event :rate 0.3)))

      this))


  (render
    [this pixel]
    (let [color-arc (/ sphere/tau (count colors))
          pixel-theta (-> pixel :barrel :theta)
          theta' (+ pixel-theta (:theta this))
          theta' (mod theta' sphere/tau)
          color-index (int (/ theta' color-arc))]
      (get colors color-index))))


(defn init
  "Creates a new beachball mode with starting speed and seq of colors."
  ([colors]
   (init (/ sphere/tau 4) colors))
  ([speed colors]
   (BeachballMode. 0 speed colors)))
