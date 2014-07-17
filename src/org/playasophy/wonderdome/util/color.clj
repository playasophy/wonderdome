(ns org.playasophy.wonderdome.util.color
  "Functions for manipulating and generating color values.
  See: http://en.wikipedia.org/wiki/HSL_and_HSV"
  (import
    java.awt.Color))


(defn color?
  "Tests whether the given value is a valid color."
  [value]
  (and (integer? value)
       (< Integer/MIN_VALUE value Integer/MAX_VALUE)))



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
  {:pre [(color? color)]}
  (vec (.getRGBColorComponents (Color. (unchecked-int color)) nil)))



;;;;; HSV COLOR SPACE ;;;;;

(defn hsv
  "Creates a color value from a hue, saturation, and value. The fractional
  portion of the hue determines the angle in the HSV space; saturation and
  value should be numbers from [0.0, 1.0].

  The primary red, green, and blue hues are found at 0/3, 1/3, and 2/3,
  respectively."
  [h s v]
  (Color/HSBtoRGB (float h) (float s) (float v)))


(defn hsv-components
  "Takes a color value and returns a vector containing the hue, saturation, and
  value components as floating-point numbers."
  [color]
  {:pre [(color? color)]}
  (let [c (Color. (unchecked-int color))]
    (vec (Color/RGBtoHSB (.getRed c) (.getGreen c) (.getBlue c) nil))))



;;;;; HSL COLOR SPACE ;;;;;

(defn hsl
  "Creates a color value from a hue, saturation, and lightness. This is similar
  to HSV, except that the RGB color space is projected into a bicone instead of
  a cone. This means a lightness of 0.0 is black, as with HSV, but 1.0 gives
  white instead of a 'fully bright' color."
  [h s l]
  (let [l' (* 2 l)
        s' (* s (if (> l' 1) (- 2 l') l'))
        s-div (+ l' s')
        s' (if (zero? s-div) 0.0 (/ (* 2 s') s-div))
        v' (/ (+ l' s') 2)]
    (hsv h s' v')))


(defn hsl-components
  "Takes a color value and returns a vector containing the hue, saturation, and
  lightness components as floating-point numbers."
  [color]
  {:pre [(color? color)]}
  (let [[h s v] (hsv-components color)
        l' (* v (- 2 s))
        s-div (if (> l' 1) (- 2 l') l')
        s' (if (zero? s-div) 0.0 (/ (* v s) s-div))
        l' (/ l' 2)]
    [h s' l']))



;;;;; COLOR GRADIENTS ;;;;;

(defn blend-rgb
  "Blends two colors by linearly interpolating their red, green, and blue
  channels by a certain proportion."
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
  "Blends two colors by linearly interpolating their hue, saturation, and value
  channels by a certain proportion. The direction the hue is interpolated may
  be controlled with an optional mode argument. Mode may be :closest, :pos, or
  :neg. The default is :closest."
  ([p x y]
   (blend-hsv :closest p x y))
  ([mode p x y]
   {:pre [(keyword? mode) (<= 0.0 p 1.0)]}
   (let [[xh xs xv] (hsv-components x)
         [yh ys yv] (hsv-components y)
         dh+ (- (if (< yh xh) (+ yh 1.0) yh) xh)
         dh- (- 1.0 dh+)]
     (hsv
       (case mode
         :pos (+ xh (* p dh+))
         :neg (- xh (* p dh-))
         :closest
         (if (< dh+ dh-)
           (+ xh (* p dh+))
           (- xh (* p dh-))))
       (+ xs (* p (- ys xs)))
       (+ xv (* p (- yv xv)))))))


(defn blend-cubehelix
  "Blends two colors using the cubehelix algorithm. This generates a more even
  and pleasing change in lightness across the interpolation.
  See: http://bl.ocks.org/mbostock/310c99e53880faec2434"
  ([p x y]
   (blend-cubehelix 1.0 p x y))
  ([gamma p x y]
   (let [[xh xs xl :as xc] (if (integer? x) (hsl-components x) x)
         [yh ys yl :as yc] (if (integer? y) (hsl-components y) y)
         [dh ds dl] (map - yc xc)
         h (+ xh 1/3 (* p dh))
         l (Math/pow (+ xl (* p dl)) gamma)
         s (* (+ xs (* p ds)) l (- 1 l))
         h' (* h 2 Math/PI)
         cos-h (Math/cos h')
         sin-h (Math/sin h')
         r (+ (* -0.14861 cos-h) (* 1.78277 sin-h))
         g (- (* -0.29227 cos-h) (* 0.90649 sin-h))
         b    (*  1.97294 cos-h)]
     (apply rgb (map #(-> % (* s) (+ l) (min 1.0) (max 0.0)) [r g b])))))


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
    (blend-hsv t (nth colors t0) (nth colors t1))))
