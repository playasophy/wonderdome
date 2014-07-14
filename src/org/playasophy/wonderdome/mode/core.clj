(ns org.playasophy.wonderdome.mode.core)


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



;;;;; HELPER FUNCTIONS ;;;;;

(defn color
  "Creates a color number compatible with Processing and PixelPusher hardware.
  Each color channel should be an 8-bit value from 0 to 255. If not set, the
  alpha channel defaults to full opacity (255)."
  ([r g b]
   (color r g b 255))
  ([r g b a]
   (unchecked-int
     (+ (bit-shift-left a 24)
        (bit-shift-left r 16)
        (bit-shift-left g  8)
                        b))))


(defn gradient
  "Assigns a color based on the position through a gradient of color points."
  ; TODO: don't require a blend-color function, do it locally.
  [blend-color colors value]
  (let [s  (count colors)
        t  (* (- value (int value)) s)
        t0 (int t)
        t1 (if (>= (inc t0) s) 0 (inc t0))
        p  (- t t0)]
    (blend-color (colors t0) (colors t1) p)))


; TODO: less-angry rainbow!
