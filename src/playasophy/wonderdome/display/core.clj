(ns playasophy.wonderdome.display.core
  "Displays are pixel color outputs. A display may run its own thread to show
  output; Processing runs a rendering loop, and pixel-pusher runs a UDP
  broadcast thread.")


(defprotocol Display
  "A protocol for displaying visualization output."

  (set-colors!
    [display colors]
    "Sets the color of the pixels in the display. The colors should be a
    sequence of strips, where each strip is a sequence of colors to set at each
    pixel index (or nil to leave the color unchanged)."))


(defn clear
  "Turns off a display by writing black to every pixel."
  [display]
  (set-colors! display (repeat 100 (repeat 1000 0))))
