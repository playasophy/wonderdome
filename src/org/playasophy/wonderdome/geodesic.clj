(ns org.playasophy.wonderdome.geodesic
  (:require
    [quil.core :refer :all]
    [quil.helpers.drawing :refer [line-join-points]]))


(def strut-counts-3V
  "Number of each strut needed in a 3V dome."
  {:a 30
   :b 40
   :c 50})


(def strut-ratios-3V
  "The ratio of each strut length to the radius."
  {:a 0.34862
   :b 0.40355
   :c 0.41241})


(defn- to-unit-radius
  "Projects a point to the same vector on the surface of the unit sphere."
  [p]
  (let [m (apply mag p)]
    (vec (map #(/ % m) p))))


; TODO: find a way to generate this mathematically
(def icosahedron
  "The set of faces making up an icosahedron with unit radius. Each face is a
  set of three coordinate vectors."
  (let [phi (/ (+ 1 (Math/sqrt 5.0)) 2)
        -phi (- phi)
        points
        {:A [ 0.0  1.0  phi]
         :B [ 0.0 -1.0  phi]
         :C [ 0.0 -1.0 -phi]
         :D [ 0.0  1.0 -phi]
         :E [ phi  0.0  1.0]
         :F [-phi  0.0  1.0]
         :G [-phi  0.0 -1.0]
         :H [ phi  0.0 -1.0]
         :I [ 1.0  phi  0.0]
         :J [-1.0  phi  0.0]
         :K [-1.0 -phi  0.0]
         :L [ 1.0 -phi  0.0]}
        faces
        [[:A :I :J] [:A :J :F] [:A :F :B] [:A :B :E] [:A :E :I]
         [:B :F :K] [:B :K :L] [:B :L :E] [:C :D :H] [:C :H :L]
         [:C :L :K] [:C :K :G] [:C :G :D] [:D :G :J] [:D :J :I]
         [:D :I :H] [:E :L :H] [:E :H :I] [:F :J :G] [:F :G :K]]]
    (set (map (comp set (partial map (comp to-unit-radius points))) faces))))


(defn- midpoint
  "Returns a new point that is a fraction of the distance towards point b
  from point a."
  [p a b]
  (->>
    (map - b a)
    (map (partial * p))
    (map + a)
    to-unit-radius))


(defn split-face
  "Splits the edges of a triangular face into n smaller segments, then pushes
  all the new points out to the surface of a unit sphere. Returns a set of new
  faces."
  [face n]
  (if (<= n 1)
    #{face}
    (let [[a b c] (seq face)]
      (cond
        (= 0 (mod n 2))
        ;       a
        ;      / \
        ;     f---d
        ;    / \ / \
        ;   c---e---b
        (let [d (midpoint 1/2 a b)
              e (midpoint 1/2 b c)
              f (midpoint 1/2 c a)]
          (->>
            [         #{a d f}
             #{f e c} #{f d e} #{d b e}]
            (mapcat #(split-face % (/ n 2)))
            set))

        (= 0 (mod n 3))
        ;       a
        ;      / \
        ;     i---d
        ;    / \ / \
        ;   h---j---e
        ;  / \ / \ / \
        ; c---g---f---b
        (let [d (midpoint 1/3 a b)
              e (midpoint 2/3 a b)
              f (midpoint 1/3 b c)
              g (midpoint 2/3 b c)
              h (midpoint 1/3 c a)
              i (midpoint 2/3 c a)
              j (midpoint 1/2 h e)]
          (->>
            [                  #{a d i}
                      #{i j h} #{i d j} #{d e j}
             #{h g c} #{h j g} #{j f g} #{j e f} #{e f b}]
            (mapcat #(split-face % (/ n 3)))
            set))

        :else
        (throw (IllegalArgumentException.
                 (str n " is not divisible by any supported prime")))))))


(defn split-faces
  [shape n]
  (set (mapcat #(split-face % n) shape)))


(defn- face->lines
  "Converts a face (set of three points) into a sequence of edge lines."
  [face]
  (as-> face points
    (vec points)
    (conj points (first points))
    (partition 2 points)))



;;;;; SKETCH CODE ;;;;;

(def dome-faces
  (split-faces icosahedron 3))


(defn setup []
  (background 255)
  (stroke 00))


(defn draw []
  (background 255)
  (translate (/ (width) 2) (/ (height) 2) 0)
  (rotate-y (* (frame-count) 0.01))
  ;(rotate-x (* (frame-count) 0.04))
  (doseq [face dome-faces]
    (-> face
        (as-> points
          (map #(vec (map (partial * 100) %)) points)   ; scale the points
          (vec points)
          (conj points (first points)))                ; repeat the last point
        line-join-points
        (as-> lines
          (map (partial apply line) lines))
        dorun)))


(defsketch geodesic-dome
  :title "Geodesic Dome"
  :setup setup
  :draw draw
  :size [500 300]
  :renderer :opengl)
