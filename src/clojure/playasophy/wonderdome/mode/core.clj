(ns playasophy.wonderdome.mode.core
  (:refer-clojure :exclude [update]))


(defprotocol Mode
  "A protocol for visualizations running on the Wonderdome."

  (update
    [mode event]
    "Computes an updated mode state from an input event. Events may be button
    presses, time-deltas, audio frames, etc. Returns the new mode state.")

  (render
    [mode pixel]
    "Queries the mode for the color which should be assigned to a given pixel.
    The pixel is a map which gives the strip and pixel index, along with the
    spherical and cartesian coordinates assigned to the pixel by the layout."))
