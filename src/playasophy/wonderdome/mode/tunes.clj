(ns playasophy.wonderdome.mode.tunes
  (:require
    [playasophy.wonderdome.geometry.sphere :as sphere]
    [playasophy.wonderdome.mode.core :as mode]
    [playasophy.wonderdome.util.color :as color]))


(defrecord TunesMode
  [gain falloff]

  mode/Mode

  (update
    [this event]
    (case (:type event)
      ; TODO: add controls for adjusting gain and falloff

      :audio/beat
      (assoc this
             :beat/at (System/currentTimeMillis)
             :beat/power (:power event))

      :audio/freq
      (assoc this
             :freq/spectrum (:spectrum event)
             :freq/at (System/currentTimeMillis))

      ; default
      this))


  (render
    [this pixel]
    (let [[r p a] (:sphere pixel)
          now (System/currentTimeMillis)
          beat-elapsed (- now (or (:beat/at this) 0))
          saturation (Math/exp (/ beat-elapsed -100.0))]
      (color/hsv
        a ;(/ a sphere/tau)
        1
        1))))


(defn tunes
  "Creates a new rainbow color-cycling mode."
  []
  (TunesMode. 0.0 0.0))
