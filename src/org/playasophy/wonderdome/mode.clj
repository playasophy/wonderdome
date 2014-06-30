(ns org.playasophy.wonderdome.mode)


(defprotocol Mode
  "A protocol for visualizations running on the Wonderdome."

  (update!
    [mode dt events]
    "Computes an updated mode state from the current state, an elapsed time in
    millseconds, and a sequence of input events.")

  (render-pixel
    [mode coordinate]
    "Queries the mode for the color which should be assigned to a given pixel.
    The coordinate is a map which gives the strip and pixel index, along with
    the spherical coordinates assigned to the pixel by the layout."))
