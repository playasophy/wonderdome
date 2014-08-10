(ns org.playasophy.wonderdome.mode.tunes
  (:require
    [org.playasophy.wonderdome.geometry.sphere :as sphere]
    [org.playasophy.wonderdome.mode.core :as mode]
    [org.playasophy.wonderdome.util.color :as color]))


(def ^:private ^:const move-rate
  "Rate at which the angles change in radians per millisecond."
  0.0005)


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
          beat-elapsed (- now (:beat/at this))
          saturation (Math/exp (/ beat-elapsed -100.0))]
      (color/hsv
        (/ a sphere/tau)
        (if (neg? a) 0.0 1.0)
        1.0))))


(defn tunes
  "Creates a new rainbow color-cycling mode."
  []
  (TunesMode. 0.0 0.0))
