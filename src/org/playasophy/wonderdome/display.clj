(ns org.playasophy.wonderdome.display)


(defprotocol Display
  "A protocol for displaying visualization output."

  (set-pixel!
    [display pixel color]
    "Sets the color of a pixel in this display."))
