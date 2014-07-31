(ns org.playasophy.wonderdome.mode.strobe
  (:require
    [org.playasophy.wonderdome.mode.core :as mode]))


(defrecord StrobeMode
  [colors index]

  mode/Mode

  (update
    [this event]
    (if (= :time/tick (:type event))
      (assoc this :index
        (mod (inc index) (count colors)))
      this))


  (render
    [this pixel]
    (nth colors index)))


(defn strobe
  "Creates a new strobe mode with the given color sequence."
  [colors]
  (StrobeMode. (vec colors) 0))
