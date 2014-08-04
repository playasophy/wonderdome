(ns org.playasophy.wonderdome.util.quil
  "Utility functions for quil/processing sketches."
  (:require
    (org.playasophy.wonderdome.geometry
      [cartesian :as cartesian])
    [quil.core :as quil]))


(defn scale-point
  "Scales up a point vector to screen space."
  [p]
  (let [s (/ (min (quil/width) (quil/height)) 10.0)]
    (cartesian/scale p s)))


(defn draw-axes
  [length]
  (quil/stroke-weight 1)
  ; x-axis red
  (quil/stroke (quil/color 255 0 0))
  (quil/line [0 0 0] (scale-point [length 0 0]))
  ; y-axis green
  (quil/stroke (quil/color 0 255 0))
  (quil/line [0 0 0] (scale-point [0 length 0]))
  ; z-axis blue
  (quil/stroke (quil/color 0 0 255))
  (quil/line [0 0 0] (scale-point [0 0 length])))


(defn draw-ground
  [radius]
  (let [c (quil/color 210 200 175)
        r (* scale radius)]
    (quil/fill c)
    (quil/stroke c)
    (quil/ellipse 0 0 r r)))
