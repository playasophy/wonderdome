(ns org.playasophy.wonderdome.util.color
  "Functions for manipulating and generating color values.
  See: http://en.wikipedia.org/wiki/HSL_and_HSV"
  (import
    java.awt.Color))


;;;;; RGB COLOR SPACE ;;;;;

(defn rgb*
  "Creates a color value from red, green, and blue component channels. Each
  component should be an 8-bit value from [0, 255]."
  [r g b]
  (.getRGB (Color. (int r) (int g) (int b))))


(defn rgb
  "Creates a color value from red, green, and blue component channels. Each
  component should be a number from [0.0, 1.0]."
  [r g b]
  (.getRGB (Color. (float r) (float g) (float b))))


(defn rgb-components
  "Takes a color value and returns a vector containing the red, green, and blue
  component channels as floating-point numbers."
  [color]
  (vec (.getRGBColorComponents (Color. (unchecked-int color)) nil)))



;;;;; HSV COLOR SPACE ;;;;;

(defn hsv
  "Creates a color value from a hue, saturation, and value. The fractional
  portion of the hue determines the angle in the HSV space; saturation and
  value should be numbers from [0.0, 1.0]."
  [h s v]
  ; TODO: document hue angles for pure red/green/blue
  (Color/HSBtoRGB (float h) (float s) (float v)))


(defn hsv-components
  "Takes a color value and returns a vector containing the hue, saturation, and
  value components as floating-point numbers."
  [color]
  (let [c (Color. (unchecked-int color))]
    (vec (Color/RGBtoHSB (.getRed c) (.getBlue c) (.getGreen c) nil))))



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
