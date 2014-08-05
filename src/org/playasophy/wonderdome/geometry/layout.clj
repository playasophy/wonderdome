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


(defn- place-segments
  "Given a sequence of struts and a sequence of pixel counts per segment,
  returns a sequence of coordinates for each pixel."
  [struts counts pixel-spacing]
  ; TODO: implement
  nil)


(defn geodesic
  "Constructs a new geodesic layout with the given dimensions."
  [{:keys [radius pixel-spacing]}]
  (let [A 1.320
        B 1.528
        C 1.562
        pixel-pair 0.041
        pairs-per-strip 120
        connector-length 0.25
        A-pairs 27
        B-pairs 32
        C-pairs 33
        strip-pairs [24 32 32 32]
        strip-struts
        [[0 6 12 10]
         [2 8 18 16]
         [4 9 17 19]
         [3 7 11 13]
         [1 5 14 15]]
        ]
    ; TODO: function which takes a sequence of struts (and the sorted edge
    ; sequence) and returns a vector of struts, sorted such that adjacent
    ; points are near each other. Then the count of pixels per strut can be
    ; mapped onto the sorted strut endpoints and concatenated together.
    nil))
