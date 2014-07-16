(ns org.playasophy.wonderdome.util.color
  "Functions for manipulating and generating color values.

  See: http://en.wikipedia.org/wiki/HSL_and_HSV")


;;;;; HELPER FUNCTIONS ;;;;;

(defn float->byte
  "Converts a number in the range [0.0, 1.0] to an integer in the range [0,
  255]. Values outside this range will be clamped."
  [value]
  {:pre [(number? value)]}
  (-> value (* 255) int (bit-and 0xFF)))


(defn byte->float
  "Converts a byte in the range [0, 255] to a floating-point value in the range
  [0.0, 1.0]."
  [value]
  (-> value (bit-and 0xFF) (/ 255.0)))



;;;;; RGB COLOR SPACE ;;;;;

(defn rgb*
  "Creates a color value compatible with Processing and PixelPusher hardware.
  Each color channel should be an 8-bit value from 0 to 255. If not set, the
  alpha channel defaults to full opacity (255)."
  ([r g b]
   (rgb* r g b 255))
  ([r g b a]
   {:pre [(integer? r) (integer? g) (integer? b) (integer? a)]}
   (unchecked-int
     (+ (bit-shift-left (bit-and 0xFF a) 24)
        (bit-shift-left (bit-and 0xFF r) 16)
        (bit-shift-left (bit-and 0xFF g)  8)
                        (bit-and 0xFF b)))))


(defn rgb
  "Creates a color value from red, green, and blue channel values. Each value
  should be a number from [0.0, 1.0]."
  [r g b]
  {:pre [(number? r) (number? g) (number? b)]}
  (rgb*
    (float->byte r)
    (float->byte g)
    (float->byte b)))


(defn red
  "Extracts the red channel from a color value. Returns a floating-point value
  from [0.0, 1.0]."
  [color]
  {:pre [(integer? color)]}
  (-> color (bit-shift-right 16) byte->float))


(defn green
  "Extracts the green channel from a color value. Returns a floating-point
  value from [0.0, 1.0]."
  [color]
  {:pre [(integer? color)]}
  (-> color (bit-shift-right 8) byte->float))


(defn blue
  "Extracts the blue channel from a color value. Returns a floating-point value
  from [0.0, 1.0]."
  [color]
  {:pre [(integer? color)]}
  (byte->float color))



;;;;; HSV COLOR SPACE ;;;;;

(defn hsv
  "Creates a color value from a hue, saturation, and value. Hue is an angle in
  [0.0, 2*PI], and saturation and value should be numbers from [0.0, 1.0]."
  [h s v]
  ; TODO: implement HSV -> RGB conversion
  nil)





;;;;; GRADIENT FUNCTIONS ;;;;;

(defn gradient   ; FIXME
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
