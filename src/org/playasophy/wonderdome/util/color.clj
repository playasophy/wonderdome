(ns org.playasophy.wonderdome.util.color
  "Functions for manipulating and generating color values.
  See: http://en.wikipedia.org/wiki/HSL_and_HSV"
  (import
    java.awt.Color))

; TODO: use deftypes instead of vectors with metadata and protocols instead of multimethods
; should be much higher performance


;;;;; HELPER FUNCTIONS ;;;;;

(defn color-number?
  "Tests whether the given value is a valid numeric color, storing
  the red, green, and blue color channels."
  [value]
  (and (integer? value)
       (< Integer/MIN_VALUE value Integer/MAX_VALUE)))


(defn color-vector?
  "Tests whether the given value is a valid color vector, containing at least
  three color channels and :type metadata indicating :rgb, :hsv, or :hsl mode."
  [value]
  (and (vector? value)
       (>= 3 (count value))
       (contains? #{:rgb :hsv :hsl} (type value))))


(defn color?
  "Tests whether the given value is a valid color."
  [value]
  (or (color-number? value)
      (color-vector? value)))


(defmulti pack
  "Converts a color to a numeric color value."
  type)


(defmethod pack Number
  [value]
  {:pre [(integer? value) (<= Integer/MIN_VALUE value Integer/MAX_VALUE)]}
  value)



;;;;; RGB COLOR SPACE ;;;;;

(defn rgb*
  "Creates a color vector with red, green, and blue components."
  [r g b]
  {:pre [(number? r) (number? g) (number? b)]}
  (vary-meta
    [(max 0.0 (min 1.0 (float r)))
     (max 0.0 (min 1.0 (float g)))
     (max 0.0 (min 1.0 (float b)))]
    assoc :type :rgb))


(defn rgb
  "Creates a numeric color value from red, green, and blue component channels.
  Each component should be a number from [0.0, 1.0]. Values outside this range
  are clamped to the bounds."
  [r g b]
  {:pre [(number? r) (number? g) (number? b)]}
  (.getRGB (Color. (max 0.0 (min 1.0 (float r)))
                   (max 0.0 (min 1.0 (float g)))
                   (max 0.0 (min 1.0 (float b))))))


(defn gray
  "Creates a numeric grayscale color by using the same value for every color
  channel. The luminance should be a value from [0.0, 1.0]."
  [l]
  (rgb l l l))


(defmethod pack :rgb
  [[r g b]]
  (rgb r g b))


(defmulti rgb-vec
  "Converts a color to a vector of red, green, and blue components."
  type)


(defmethod rgb-vec Number
  [value]
  {:pre [(color-number? value)]}
  (apply rgb* (-> value unchecked-int Color. (.getRGBColorComponents nil))))


(defmethod rgb-vec :rgb
  [value]
  value)


(defmethod rgb-vec :default
  [value]
  (rgb-vec (pack value)))



;;;;; HSV COLOR SPACE ;;;;;

(defn hsv*
  "Creates a color vector with hue, saturation, and value components."
  [h s v]
  {:pre [(number? h) (number? s) (number? v)]}
  (vary-meta
    [h s v]
    assoc :type :hsv))


(defn hsv
  "Creates a color value from a hue, saturation, and value. The fractional
  portion of the hue determines the angle in the HSV space; saturation and
  value should be numbers from [0.0, 1.0].

  The primary red, green, and blue hues are found at 0/3, 1/3, and 2/3,
  respectively."
  [h s v]
  {:pre [(number? h) (number? s) (number? v)]}
  (Color/HSBtoRGB
    (float h)
    (max 0.0 (min 1.0 (float s)))
    (max 0.0 (min 1.0 (float v)))))


(defmethod pack :hsv
  [[h s v]]
  (hsv h s v))


(defmulti hsv-vec
  "Converts a color to a vector of hue, saturation, and value components."
  type)


(defmethod hsv-vec Number
  [value]
  {:pre [(color-number? value)]}
  (let [color (Color. (unchecked-int value))]
    (apply hsv* (Color/RGBtoHSB
                  (.getRed color)
                  (.getGreen color)
                  (.getBlue color)
                  nil))))


(defmethod hsv-vec :hsv
  [value]
  value)


(defmethod hsv-vec :default
  [value]
  (hsv-vec (pack value)))



;;;;; HSL COLOR SPACE ;;;;;

(defn hsl*
  "Creates a color vector with hue, saturation, and lightness components."
  [h s l]
  {:pre [(number? h) (number? s) (number? l)]}
  (vary-meta
    [h s l]
    assoc :type :hsl))


(defn hsl
  "Creates a color value from a hue, saturation, and lightness. This is similar
  to HSV, except that the RGB color space is projected into a bicone instead of
  a cone. This means a lightness of 0.0 is black, as with HSV, but 1.0 gives
  white instead of a 'fully bright' color."
  [h s l]
  ; TODO: double-check this calculation
  (let [l' (- (* 2 l) 1)
        l' (* 2 l')
        s' (* s (if (> l' 1) (- 2 l') l'))
        s-div (+ l' s')
        s' (if (zero? s-div) 1.0 (/ (* 2 s') s-div))
        v' (/ (+ l' s') 2)]
    (hsv h s' v')))


(defmethod pack :hsl
  [[h s l]]
  (hsl h s l))


(defmulti hsl-vec
  "Converts a color to a vector of hue, saturation, and lightness components."
  type)


(defmethod hsl-vec :hsl
  [value]
  value)


(defmethod hsl-vec :default
  [value]
  (let [[h s v] (hsv-vec value)
        l' (* v (- 2 s))
        s-div (if (> l' 1) (- 2 l') l')
        s' (if (zero? s-div) 0.0 (/ (* v s) s-div))
        l' (/ l' 2)]
    (hsl* h s' l')))



;;;;; COLOR BLENDING ;;;;;

(defn blend-rgb
  "Blends two colors by linearly interpolating their red, green, and blue
  channels by a certain proportion."
  [p x y]
  {:pre [(<= 0.0 p 1.0)]}
  (let [xc (rgb-vec x)
        yc (rgb-vec y)]
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
   (let [[xh xs xv] (hsv-vec x)
         [yh ys yv] (hsv-vec y)
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
   {:pre [(pos? gamma) (<= 0.0 p 1.0)]}
   (let [[xh xs xl :as xc] (hsl-vec x)
         [yh ys yl :as yc] (hsl-vec y)
         [dh ds dl] (map - yc xc)
         h (+ xh 1/3 (* p dh))
         l (Math/pow (+ xl (* p dl)) gamma)
         s (* (+ xs (* p ds)) l (- 1 l))
         hr (* h 2 Math/PI)
         cos-h (Math/cos hr)
         sin-h (Math/sin hr)]
     (rgb
       (+ l (* s (+ (* -0.14861 cos-h) (* 1.78277 sin-h))))
       (+ l (* s (- (* -0.29227 cos-h) (* 0.90649 sin-h))))
       (+ l (* s    (*  1.97294 cos-h)))))))



;;;;; CUBEHELIX RAINBOW ;;;;;

(def ^:private ^:const rainbow-points
  "Points on the cubehelix rainbow."
  [(hsl* -5/18 0.75 0.35)
   (hsl*  4/18 1.50 0.80)
   (hsl* 13/18 0.75 0.35)])


(defn rainbow
  "Maps a floating point value to a color in the cubehelix rainbow. The
  fractional part of the number determines the position in the gradient."
  [value]
  (let [p (- value (Math/floor value))]
    (if (< p 0.5)
      (blend-cubehelix (* 2 p)
        (rainbow-points 0)
        (rainbow-points 1))
      (blend-cubehelix (* 2 (- p 0.5))
        (rainbow-points 1)
        (rainbow-points 2)))))
