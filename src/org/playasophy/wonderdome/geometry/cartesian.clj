(ns org.playasophy.wonderdome.geometry.cartesian
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
     (Math/acos (/ z r))
     (Math/atan (/ y x))]))


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


(defn dedupe-edges
  "Deduplicates a set of edges by unifying point values which lie within a certain
  distance epsilon of each other."
  [epsilon edges]
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
            result' (conj result (vec (sort [a' b'])))]
        (if (next edges)
          (recur points' (next edges) result')
          result')))))


(defn sort-edges
  "Returns a sequence of edges, sorted by z, y, then x coordinates."
  [edges]
  (let [edge-key (fn [[a b]] (vec (map sort (reverse (map vector a b)))))]
    (reverse (sort-by edge-key edges))))
