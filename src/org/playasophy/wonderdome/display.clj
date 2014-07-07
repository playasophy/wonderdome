(ns org.playasophy.wonderdome.display)


(defprotocol Display
  "A protocol for displaying visualization output."

  (set-colors!
    [display colors]
    "Sets the color of the pixels in the display. The colors should be a vector
    of strips, where each strip is a vector of colors to set at each pixel
    index (or nil to leave the color unchanged)."))
