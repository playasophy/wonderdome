(ns org.playasophy.wonderdome.layout)


;;;;; LAYOUT PROTOCOL ;;;;;

(defprotocol Layout
  "Layouts map pixels to physical locations in a spherical coordinate system."

  (place
    [this strip pixel]
    "Returns a spherical coordinate map for the given strip and pixel indexes.
    Maps should contain a :radius in meters, and :polar and :azimuth angles in
    radians."))



;;;;; HELPER FUNCTIONS ;;;;;

(def PI  Math/PI)
(def TAU (* 2 PI))


(defn wrap-angle
  "Wraps an angle so that it lies between -pi and pi radians."
  [angle]
  (let [wraps (-> angle (/ TAU) Math/abs Math/floor (* TAU))]
    (cond
      (neg? angle) (let [a (+ angle wraps)]
                      (if (< a (- PI))
                        (+ a TAU)
                        a))
      (pos? angle) (let [a (- angle wraps)]
                      (if (> a PI)
                        (- a TAU)
                        a))
      :else 0.0)))


(defn normalize-coord
  "Normalizes a spherical coordinate so that the polar angle is between 0 and
  pi radians, and the azimuthal angle is between -pi and pi."
  [{:keys [radius polar azimuth]}]
  (let [polar' (wrap-angle polar)
        [polar' azimuth'] (if (neg? polar')
                            [(- polar') (+ azimuth PI)]
                            [polar' azimuth])
        azimuth' (wrap-angle azimuth')]
    {:radius radius
     :polar polar'
     :azimuth azimuth'}))
