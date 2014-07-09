(ns org.playasophy.wonderdome.layout
  "Layouts map pixels to spherical coordinate map for the given strip and pixel
  indexes. Maps should contain a :radius in meters, and :polar and :azimuth
  angles in radians."
  (:require
    (org.playasophy.wonderdome.geometry
      [cartesian :as cartesian]
      [sphere :as sphere :refer [tau]])))


;;;;; HELPER FUNCTIONS ;;;;;

(defn place-pixels
  "Takes a placement function and maps it over a number of strips and pixels,
  returning a vector of vectors of pixel coordinates. The placement function
  should accept a strip and pixel index as the first and second arguments and
  return a map with either a :sphere coordinate vector or a cartesian :coord
  vector. The other vector will be generated, and the strip and pixel indices
  added."
  [strips pixels f]
  (let [range-mapvec (fn [r g] (vec (map g (range r))))]
    (range-mapvec strips
      (fn [s]
        (range-mapvec pixels
          (fn [p]
            (let [place (f s p)]
              (cond
                (:sphere place)
                (assoc place
                  :strip s
                  :pixel p
                  :coord (sphere/->cartesian (:sphere place)))

                (:coord place)
                (assoc place
                  :strip s
                  :pixel p
                  :sphere (cartesian/->sphere (:coord place)))

                :else
                (throw (IllegalStateException.
                         (str "Placement function did not return either a cartesian or spherical coordinate: "
                              (pr-str place))))))))))))



;;;;; SIMPLE LAYOUTS ;;;;;

(defn star
  "Constructs a new star layout with the given dimenisons."
  [{:keys [radius pixel-spacing strips strip-pixels]}]
  (let [strip-angle (/ tau strips)
        pixel-angle (* 2 (Math/asin (/ pixel-spacing 2 radius)))]
    (place-pixels
      strips strip-pixels
      (fn [strip pixel]
        {:sphere
         [radius
          (* pixel-angle (+ pixel 5))
          (* strip-angle strip)]}))))
