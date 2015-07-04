(ns playasophy.wonderdome.geometry.sphere
  "Functions for working in the spherical coordinate system. A point is a
  3-element vector of [radius polar azimuth]. Radius is assumed to be in
  meters, and angles in radians.")


(def pi  Math/PI)
(def tau (* 2 pi))


(defn wrap-angle
  "Wraps an angle so that it lies between -pi and pi radians."
  [angle]
  (let [wraps (-> angle (/ tau) double Math/abs Math/floor (* tau))]
    (cond
      (neg? angle)
      (let [a (+ angle wraps)]
        (if (< a (- pi))
          (+ a tau)
          a))

      (pos? angle)
      (let [a (- angle wraps)]
        (if (> a pi)
          (- a tau)
          a))

      :else 0.0)))


(defn normalize
  "Normalizes a spherical coordinate so that the polar angle is between 0 and
  pi radians, and the azimuthal angle is between -pi and pi."
  [[radius polar azimuth]]
  (let [polar' (wrap-angle polar)
        [polar' azimuth'] (if (neg? polar')
                            [(- polar') (+ azimuth pi)]
                            [polar' azimuth])
        azimuth' (wrap-angle azimuth')]
    [radius polar' azimuth']))


(defn ->cartesian
  "Converts a spherical [r p a] vector to cartesian [x y z] coordinates."
  [[radius polar azimuth]]
  [(* radius (Math/sin polar) (Math/cos azimuth))
   (* radius (Math/sin polar) (Math/sin azimuth))
   (* radius (Math/cos polar))])


(defn angle-offset
  "Calculates the great-circle angle between two spherical coordinates. Ignores
  the radius."
  [[_ polar1 azimuth1] [_ polar2 azimuth2]]
  (Math/acos
    (+ (* (Math/cos polar1)
          (Math/cos polar2))
       (* (Math/sin polar1)
          (Math/sin polar2)
          (Math/cos (- azimuth1 azimuth2))))))
