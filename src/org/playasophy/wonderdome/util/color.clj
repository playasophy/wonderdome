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


(defn gray
  "Creates a grayscale color by using the same value for every color channel."
  [v]
  (rgb v v v))


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



;;;;; COLOR GRADIENTS ;;;;;

(defn blend-rgb
  "Generates a color which is composed of linearly interpolated red, green, and
  blue channels between two colors."
  [p x y]
  {:pre [(<= 0.0 p 1.0)]}
  (let [xc (rgb-components x)
        yc (rgb-components y)]
    (->>
      (map - yc xc)
      (map (partial * p))
      (map + xc)
      (apply rgb))))


(defn blend-hsv
  "Generates a color which is composed of linearly interpolated hue,
  saturation, and value channels between two colors."
  [p x y]
  {:pre [(<= 0.0 p 1.0)]}
  (let [xc (hsv-components x)
        yc (hsv-components y)]
    ; FIXME: this doesn't work because hue may wrap around the circle
    (->>
      (map - yc xc)
      (map (partial * p))
      (map + xc)
      (apply hsv))))


(defn gradient   ; FIXME
  "Assigns a color based on the position through a gradient of color points.
  Each color in the sequence occupies an equal amount of space in the cycle,
  such that p of 0.0 and 1.0 both give the first color in the sequence."
  [colors p]
  (let [s  (count colors)
        t  (* (- p (Math/floor p)) s)
        t0 (int t)
        t1 (if (>= (inc t0) s) 0 (inc t0))
        p  (- t t0)]
    (blend t (nth colors t0) (nth colors t1))))


; TODO: less-angry rainbow!
