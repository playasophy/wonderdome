(ns playasophy.wonderdome.mode.strobe
  (:require
    [playasophy.wonderdome.mode.core :as mode]
    [playasophy.wonderdome.util.control :as control]))


(defrecord StrobeMode
  [colors index frames-per-color frame-index]

  mode/Mode

  (update
    [this event]
    (condp = [(:type event) (:input event)]
      [:time/tick nil]
      (if (>= frame-index frames-per-color)
        (assoc this
               :index (mod (inc index) (count colors))
               :frame-index 1)
        (assoc this
               :frame-index (inc frame-index)))

      [:button/press :up]
      (assoc this :frames-per-color (min (inc frames-per-color) 100))

      [:button/press :down]
      (assoc this :frames-per-color (max (dec frames-per-color) 1))

      this))


  (render
    [this pixel]
    (nth colors index)))


(defn init
  "Creates a new strobe mode with the given color sequence."
  [colors]
  (StrobeMode. (vec colors) 0 1 0))
