(ns playasophy.wonderdome.mode.strobe
  (:require
    [playasophy.wonderdome.util.control :as control])
  (:import
    playasophy.wonderdome.mode.Mode))


(def frames-per-color-bounds
  [1 30])


(defrecord StrobeMode
  [colors index frames-per-color frame-index]

  Mode

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

      [:axis/direction :y-axis]
      (assoc this :frames-per-color
             (control/adjust frames-per-color event
                             :rate -1.0
                             :min-val (first frames-per-color-bounds)
                             :max-val (second frames-per-color-bounds)))

      [:button/press :L]
      (assoc this :frames-per-color (second frames-per-color-bounds))

      [:button/press :R]
      (assoc this :frames-per-color (first frames-per-color-bounds))

      this))


  (render
    [this pixel]
    (nth colors index)))


(defn init
  "Creates a new strobe mode with the given color sequence."
  [colors]
  (->StrobeMode (vec colors) 0 1 0))
