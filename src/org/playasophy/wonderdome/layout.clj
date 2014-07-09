(ns org.playasophy.wonderdome.layout
  "Layouts map pixels to spherical coordinate map for the given strip and pixel
  indexes. Maps should contain a :radius in meters, and :polar and :azimuth
  angles in radians."
  (:require
    [org.playasophy.wonderdome.geometry.sphere :refer [tau]]))


;;;;; HELPER FUNCTIONS ;;;;;

(defn place-pixels
  "Takes a placement function and maps it over a number of strips and pixels,
  returning a vector of vectors of pixel coordinates. The placement function
  should accept a strip and pixel index as the first and second arguments and
  return a spherical coordinate vector."
  [strips pixels f]
  (vec (map (fn [s] (vec (map (fn [p] {:strip s, :pixel p, :sphere (f s p)})
                              (range pixels))))
            (range strips))))



;;;;; SIMPLE LAYOUTS ;;;;;

(defn star
  "Constructs a new star layout with the given dimenisons."
  [{:keys [radius pixel-spacing strips strip-pixels]}]
  (let [strip-angle (/ tau strips)
        pixel-angle (* 2 (Math/asin (/ pixel-spacing 2 radius)))]
    (place-pixels
      strips strip-pixels
      (fn [strip pixel]
        [radius
         (* pixel-angle (+ pixel 5))
         (* strip-angle strip)]))))
