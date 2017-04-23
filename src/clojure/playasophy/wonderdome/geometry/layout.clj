(ns playasophy.wonderdome.geometry.layout
  "Layouts map pixels to spatial coordinates for the given strip and pixel
  indexes. Both cartesian [x y z] and spherical [r p a] coordinates are
  supported."
  (:require
    (playasophy.wonderdome.geometry
      [cartesian :as cartesian]
      [geodesic :as geodesic]
      [graph :as graph]
      [sphere :as sphere :refer [tau]])))


;; ## Helper Functions

(defn join
  "Produce a layout by combining multiple layout vectors."
  [& layouts]
  (vec (apply concat layouts)))


(defn update-pixels
  "Apply a function to each pixel in the given layout and return the updated
  version of the layout."
  [layout f & args]
  (mapv (partial mapv #(apply f % args)) layout))


(defn group
  "Apply a group to all pixels in a layout."
  [group-name layout]
  (update-pixels layout assoc :group group-name))


(defn translate-pixel
  "Translates a single pixel in cartesian space. Updates the spherical
  coordinate to match."
  [point offset]
  (let [coord' (cartesian/translate (:coord point) offset)]
    (assoc point
      :coord coord'
      :sphere (cartesian/->sphere coord'))))


(defn translate
  "Translate a layout in cartesian space."
  [offset layout]
  (update-pixels layout translate-pixel offset))


(defn place-pixels
  "Takes a placement function and maps it over a number of strips and pixels,
  returning a vector of vectors of pixel coordinates. The placement function
  should accept a strip and pixel index as the first and second arguments and
  return a map with either a :sphere coordinate vector or a cartesian :coord
  vector. The other vector will be generated, and the strip and pixel indices
  added."
  [strips pixels f]
  (let [maprv (fn [r g] (mapv g (range r)))]
    (maprv strips
      (fn [s]
        (maprv pixels
          (fn [p]
            (let [place (f s p)]
              (->
                (cond
                  (:sphere place)
                    (assoc place :coord (sphere/->cartesian (:sphere place)))

                  (:coord place)
                    (assoc place :sphere (cartesian/->sphere (:coord place)))

                  :else
                    (throw (IllegalStateException.
                             (str "Placement function did not return either a "
                                  "cartesian or spherical coordinate: "
                                  (pr-str place)))))
                (assoc :strip s
                       :pixel p)))))))))



;; ## Simple Layouts

(defn star
  "Constructs a new star layout with the given dimensions."
  [radius n strip]
  (let [strip-angle (/ tau n)
        pixel-angle (* 2 (Math/asin (/ (:spacing strip) 2 radius)))]
    (place-pixels
      n (:pixels strip)
      (fn [s p]
        {:sphere
         [radius
          (* pixel-angle (+ p 5))
          (* strip-angle s)]}))))


(defn barrel
  "Constructs a barrel layout with `radius` and `n` strips wound around it in a
  spiral. Each strip moves `vertical-spacing` meters down the barrel per turn."
  [radius vertical-spacing n strip]
  (let [angle-between-strips (/ tau n)
        pixel-angle (* 2 (Math/asin (/ (:spacing strip) 2 radius)))
        pixel-vertical-spacing (* vertical-spacing (/ pixel-angle tau))]
    (place-pixels
      n (:pixels strip)
      (fn [s p]
        (let [theta (+ (* s angle-between-strips) (* p pixel-angle))]
          {:coord
           [(* radius (Math/cos theta))
            (* radius (Math/sin theta))
            (- (* p pixel-vertical-spacing))]
           :barrel
           {:theta (mod theta tau)
            :z (* (- (:pixels strip) p 1) pixel-vertical-spacing)
            :normalized-z (/ p (:pixels strip))}})))))



;; ## Geodesic Layout

(defn geodesic-dome
  "Builds a sorted set of struts forming the wonder dome's geodesic frame."
  [radius]
  (into
    (sorted-set-by cartesian/edge-comparator)
    (-> (bigdec radius)
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
    (for [i (range pixels)]
      (let [p (/ (+ margin (* i spacing)) strut-length)]
        (cartesian/midpoint p a b)))))


(defn geodesic-grid
  "Constructs a new geodesic layout with the given dimensions."
  [radius & {:keys [pixel-spacing strut-pixels strip-struts]}]
  {:pre [(number? radius)
         (number? pixel-spacing)
         (sequential? strut-pixels)
         (sequential? strip-struts)]}
  (let [dome-struts (vec (geodesic-dome radius))
        struts->pixels
        (fn [strut-indexes]
          (->> strut-indexes
               (map (partial nth dome-struts))
               align-struts
               (mapcat (partial place-segment pixel-spacing) strut-pixels)))]
    (mapv
      (fn [strip i]
        (map
          (fn [coord j]
            (merge
              {:strip i
               :pixel j
               :coord coord
               :sphere (sphere/normalize (cartesian/->sphere coord))}
              (graph/strip-index->edge-offset i j)))
          strip
          (range)))
      (map struts->pixels strip-struts)
      (range))))
