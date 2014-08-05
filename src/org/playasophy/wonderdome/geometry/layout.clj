(ns org.playasophy.wonderdome.geometry.layout
  "Layouts map pixels to spatial coordinates for the given strip and pixel
  indexes. Both cartesian [x y z] and spherical [r p a] coordinates are
  supported."
  (:require
    (org.playasophy.wonderdome.geometry
      [cartesian :as cartesian]
      [geodesic :as geodesic]
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
  "Constructs a new star layout with the given dimensions."
  [& {:keys [radius pixel-spacing strips strip-pixels]}]
  (let [strip-angle (/ tau strips)
        pixel-angle (* 2 (Math/asin (/ pixel-spacing 2 radius)))]
    (place-pixels
      strips strip-pixels
      (fn [strip pixel]
        {:sphere
         [radius
          (* pixel-angle (+ pixel 5))
          (* strip-angle strip)]}))))



;;;;; GEODESIC LAYOUT ;;;;;

(defn geodesic-dome
  "Builds a sorted set of struts forming the wonder dome's geodesic frame."
  [radius]
  (into
    (sorted-set-by cartesian/edge-comparator)
    (-> radius
        (geodesic/edges 3)
        geodesic/ground-slice
        (cartesian/dedupe-edges 0.05))))


(defn- align-struts
  "Takes a sequence of struts, each of which is a vector of two cartesian
  coordinates. Returns the strut sequence, but with each strut arranged so
  that the endpoints are adjacent.

  For example, if the sequence is:
      ([B A] [C B] [C D])
  Then the result will be:
      ([A B] [B C] [C D])"
  [struts]
  (if-let [[[a b :as x] y & more] struts]
    (let [first-vertex
          (let [points (set y)]
            (or (get points a)
                (get points b)
                (throw (IllegalArgumentException.
                         (str "Strut " (pr-str x) " has no vertices in "
                              "common with " (pr-str y))))))
          first-strut (if (= first-vertex a) [b a] x)]
      (first
        (reduce
          (fn [[result vertex] [a b :as strut]]
            (cond
              (= vertex a) [(conj result [a b]) b]
              (= vertex b) [(conj result [b a]) a]
              :else
              (throw (IllegalArgumentException.
                       (str "Strut " (pr-str strut) " has no vertex matching "
                            (pr-str vertex))))))
          [[first-strut] first-vertex]
          (rest struts))))
    struts))


(defn- place-segment
  "Given a strut, a count of pixels to place on it, and a spacing between each
  pixel, returns a sequence of cartesian coordinates for each pixel."
  [spacing pixels [a b]]
  (let [strut-length (cartesian/distance a b)
        margin (/ (- strut-length (* (dec pixels) spacing)) 2)]
    (println "Placing" pixels "pixels with spacing" spacing "along" strut-length "m strut")
    (for [i (range pixels)]
      (let [p (/ (+ margin (* i spacing)) strut-length)]
        (cartesian/midpoint p a b)))))


(defn geodesic
  "Constructs a new geodesic layout with the given dimensions."
  [& {:keys [radius pixel-spacing strut-pixels strip-struts]}]
  {:pre [(number? radius)
         (number? pixel-spacing)
         (sequential? strut-pixels)
         (sequential? strip-struts)]}
  (let [dome-struts (seq (geodesic-dome radius))
        struts->pixels
        (fn [strut-indexes]
          (println "Converting strut indexes to pixels:" (pr-str strut-indexes))
          (->> strut-indexes
               (map (partial nth dome-struts))
               align-struts
               (mapcat (partial place-segment pixel-spacing) strut-pixels)))]
    (vec
      (map
        (fn [strip i]
          (map
            (fn [coord j]
              {:strip i
               :pixel j
               :coord coord
               :sphere (cartesian/->sphere coord)})
            strip
            (range)))
        (map struts->pixels strip-struts)
        (range)))))
