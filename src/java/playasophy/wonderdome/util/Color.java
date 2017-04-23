package playasophy.wonderdome.util;

import com.google.common.base.Preconditions;

public final class Color {

    public static final int NONE = 0;
    public static final int BLACK = NONE;
    public static final int WHITE = rgb(1, 1, 1);
    public static final int RED = rgb(1, 0, 0);
    public static final int GREEN = rgb(0, 1, 0);
    public static final int BLUE = rgb(0, 0, 1);
    public static final int YELLOW = rgb(1, 1, 0);
    public static final int TEAL = rgb(0, 1, 1);
    public static final int MAGENTA = rgb(1, 0, 1);
    public static int gray(float l) {
        return rgb(l, l, l);
    }

    public static int rgb(float r, float g, float b) {
        return new java.awt.Color(r, g, b).getRGB();
    }

    public static int blendRgb(float p, int x, int y) {
        Preconditions.checkArgument(0.0 <= p && p <= 1.0, "Hue must be in [0.0, 1.0]");
        // TODO: Add implementation.
        throw new UnsupportedOperationException("Not implemented");
    }

    public static int hsv(float h, float s, float v) {
        checkHsvArguments(h, s, v);
        return java.awt.Color.HSBtoRGB(h, s, v);
    }

    private static void checkHsvArguments(float h, float s, float v) {
        Preconditions.checkArgument(0.0 <= s && s <= 1.0, "Saturation must be in [0.0, 1.0]");
        Preconditions.checkArgument(0.0 <= v && v <= 1.0, "Value (a.k.a. brightness) must be in [0.0, 1.0]");
    }

    /*

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

     */

}
