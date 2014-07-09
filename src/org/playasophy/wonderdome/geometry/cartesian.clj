(ns org.playasophy.wonderdome.geometry.cartesian
  "Functions for working in the cartesian coordinate system. A point is a
  3-element vector of [x y z]. Coordinates are assumed to be in meters.")


(defn magnitude
  "Determines the magnitude of the vector from the origin to the given point."
  [p]
  (Math/sqrt (apply + (map #(* % %) p))))


(defn project-to
  "Projects a point to the same vector on the surface of a sphere with the
  given radius."
  [r p]
  (let [scale (/ r (magnitude p))]
    (vec (map (partial * scale) p))))


(defn rotate-x
  "Rotates a point around the X axis by theta radians."
  [theta [x y z]]
  (let [cos (Math/cos theta)
        sin (Math/sin theta)]
    [x
     (- (* y cos) (* z sin))
     (+ (* y sin) (* z cos))]))


(defn midpoint
  "Returns a new point that is a fraction of the distance towards point b
  from point a."
  [p a b]
  (->>
    (map - b a)
    (map (partial * p))
    (map + a)))
