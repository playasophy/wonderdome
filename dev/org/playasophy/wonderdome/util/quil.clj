(ns org.playasophy.wonderdome.util.quil
  "Utility functions for quil/processing sketches."
  (:require
    (org.playasophy.wonderdome.geometry
      [cartesian :as cartesian])
    [quil.core :as quil]))


(defn screen-scale
  "Determines how much to scale distances by based on how big the processing
  display is."
  ([]
   (/ (min (quil/width) (quil/height)) 10.0))
  ([v]
   (* v (screen-scale))))


(defn scale-point
  "Scales up a point vector to screen space."
  [p]
  (cartesian/scale p (screen-scale)))


(defn draw-axes
  [length]
  (quil/stroke-weight 1)
  (let [l (screen-scale length)]
    ; x-axis red
    (quil/stroke (quil/color 255 0 0))
    (quil/line [0 0 0] [l 0 0])
    ; y-axis green
    (quil/stroke (quil/color 0 255 0))
    (quil/line [0 0 0] [0 l 0])
    ; z-axis blue
    (quil/stroke (quil/color 0 0 255))
    (quil/line [0 0 0] [0 0 l])))


(defn draw-ground
  [radius]
  (let [c (quil/color 210 200 175)
        r (screen-scale radius)]
    (quil/fill c)
    (quil/stroke c)
    (quil/ellipse 0 0 r r)))
