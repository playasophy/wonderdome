(ns playasophy.wonderdome.mode.core
  (:refer-clojure :exclude [update])
  (:import
    playasophy.wonderdome.mode.Mode))


(defn update
  "Computes an updated mode state from an input event. Events may be button
  presses, time-deltas, audio frames, etc. Returns the new mode state."
  [^Mode mode event]
  (.update mode event))


(defn render
  "Queries the mode for the color which should be assigned to a given pixel.
  The pixel is a map which gives the strip and pixel index, along with the
  spherical and cartesian coordinates assigned to the pixel by the layout."
  [^Mode mode pixel]
  (.render mode pixel))
