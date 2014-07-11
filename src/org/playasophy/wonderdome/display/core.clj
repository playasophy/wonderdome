(ns org.playasophy.wonderdome.display.core)


(defprotocol Display
  "A protocol for displaying visualization output."

  (set-colors!
    [display colors]
    "Sets the color of the pixels in the display. The colors should be a
    sequence of strips, where each strip is a sequence of colors to set at each
    pixel index (or nil to leave the color unchanged)."))
