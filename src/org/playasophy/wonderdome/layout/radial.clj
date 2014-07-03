(ns org.playasophy.wonderdome.layout.radial)


(defn layout
  "Constructs a new radial layout with the given number of strips and pixels
  per strip."
  [{:keys [radius spacing strips pixels]}]
  (let [strip-angle (/ (* 2.0 Math/PI) strips)
        pixel-angle (* 2.0 (Math/asin (/ spacing 2.0 radius)))]
    (fn [strip pixel]
      (when (and (<= 0 strip (dec strips))
                 (<= 0 pixel (dec pixels)))
        {:radius radius
         :polar   (* pixel-angle (+ pixel 5))
         :azimuth (* strip-angle strip)}))))
