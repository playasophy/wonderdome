(ns org.playasophy.wonderdome.layout
  "Layouts map pixels to spherical coordinate map for the given strip and pixel
  indexes. Maps should contain a :radius in meters, and :polar and :azimuth
  angles in radians.")


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


(defn place-pixels
  "Takes a placement function and maps it over a number of strips and pixels,
  returning a vector of vectors of pixel coordinates. The placement function
  should accept a strip and pixel index as the first and second arguments and
  return a spherical coordinate map."
  [strips pixels f]
  (vec (map (fn [s] (vec (map (fn [p] (merge {:strip s, :pixel p} (f s p)))
                              (range pixels))))
            (range strips))))



;;;;; SIMPLE LAYOUTS ;;;;;

(defn star
  "Constructs a new star layout with the given dimenisons."
  [{:keys [radius pixel-spacing strips strip-pixels]}]
  (let [strip-angle (/ TAU strips)
        pixel-angle (* 2 (Math/asin (/ pixel-spacing 2 radius)))]
    (place-pixels
      strips strip-pixels
      (fn [strip pixel]
        {:radius  radius
         :polar   (* pixel-angle (+ pixel 5))
         :azimuth (* strip-angle strip)}))))
