(ns playasophy.wonderdome.geometry.cartesian
  "Functions for working in the cartesian coordinate system. A point is a
  3-element vector of [x y z]. Coordinates are assumed to be in meters.")


(defn scale
  "Scales a coordinate vector by a constant factor."
  [p s]
  (vec (map (partial * s) p)))


(defn magnitude
  "Determines the magnitude of the vector from the origin to the given point."
  [p]
  (Math/sqrt (apply + (map #(* % %) p))))


(defn distance
  "Determines the distance between two points."
  [a b]
  (magnitude (map - a b)))


(defn midpoint
  "Returns a new point that is a fraction of the distance towards point b
  from point a."
  [p a b]
  (->>
    (map - b a)
    (map (partial * p))
    (map + a)))


(defn ->sphere
  "Converts a cartesian coordinate to a spherical coordinate vector."
  [[x y z :as p]]
  (let [r (magnitude p)]
    [r
     (if (zero? r) 0 (Math/acos (/ z r)))
     (if (zero? x)
       (if (pos? y)
         (+ (/ Math/PI 2))
         (- (/ Math/PI 2)))
       (Math/atan (/ y x)))]))


(defn project-to
  "Projects a point to the same vector on the surface of a sphere with the
  given radius."
  [r p]
  (let [scale (/ r (magnitude p))]
    (vec (map (partial * scale) p))))


(defn rotate-x
  "Rotates a point around the X axis by theta radians."
  [theta [x y z]]
  (let [cos-t (Math/cos theta)
        sin-t (Math/sin theta)]
    [x
     (- (* y cos-t) (* z sin-t))
     (+ (* y sin-t) (* z cos-t))]))


(defn- canonize-point
  [p]
  (vec (map #(BigDecimal. % (java.math.MathContext. 5)) p)))


(defn point-comparator
  [a b]
  (compare (vec (reverse b)) (vec (reverse a))))


(defn- canonize-edge
  [e]
  (->> e (map canonize-point) (sort point-comparator) vec))


(defn edge-comparator
  [x y]
  (let [[ax bx] x
        [ay by] y
        ca (point-comparator ax ay)]
    (if (zero? ca)
      (point-comparator bx by)
      ca)))


(defn dedupe-edges
  "Deduplicates a set of edges by unifying point values which lie within a certain
  distance epsilon of each other."
  [edges epsilon]
  (let [some-point (fn [points p]
                     (or (first (filter #(< (distance p %) epsilon)
                                        points))
                         p))]
    (loop [points #{}
           edges edges
           result #{}]
      (let [[a b] (first edges)
            a' (some-point points a)
            b' (some-point points b)
            points' (conj points a' b')
            result' (conj result (canonize-edge [a' b']))]
        (if (next edges)
          (recur points' (next edges) result')
          result')))))
