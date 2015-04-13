(ns playasophy.wonderdome.geometry.geodesic
  (:require
    (playasophy.wonderdome.geometry
      [cartesian :as cartesian])))


(def phi
  "The golden ratio."
  (/ (+ 1 (Math/sqrt 5.0)) 2))


(def phi-angle
  "Angle formed by a golden rectangle."
  (Math/asin (/ (Math/sqrt (+ 1 (* phi phi))))))



;;;;; SHAPE FUNCTIONS ;;;;;

(defn- map-shape
  "Takes a shape (a set of faces, each of which is a set of points, each of
  which is a vector of 3 coordinates, and applies a function to each point."
  [f shape]
  (set (map (comp set (partial map f)) shape)))


(defn- split-face
  "Splits the edges of a triangular face into n smaller segments. Returns a set
  of new faces."
  [n face]
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
        (let [d (cartesian/midpoint 1/2 a b)
              e (cartesian/midpoint 1/2 b c)
              f (cartesian/midpoint 1/2 c a)]
          (->>
            [         #{a d f}
             #{f e c} #{f d e} #{d b e}]
            (mapcat (partial split-face (/ n 2)))
            set))

        (= 0 (mod n 3))
        ;       a
        ;      / \
        ;     i---d
        ;    / \ / \
        ;   h---j---e
        ;  / \ / \ / \
        ; c---g---f---b
        (let [d (cartesian/midpoint 1/3 a b)
              e (cartesian/midpoint 2/3 a b)
              f (cartesian/midpoint 1/3 b c)
              g (cartesian/midpoint 2/3 b c)
              h (cartesian/midpoint 1/3 c a)
              i (cartesian/midpoint 2/3 c a)
              j (cartesian/midpoint 1/2 h e)]
          (->>
            [                  #{a d i}
                      #{i j h} #{i d j} #{d e j}
             #{h g c} #{h j g} #{j f g} #{j e f} #{e f b}]
            (mapcat (partial split-face (/ n 3)))
            set))

        :else
        (throw (IllegalArgumentException.
                 (str n " is not divisible by any supported prime")))))))


(defn- split-faces
  [n shape]
  (set (mapcat (partial split-face n) shape)))


(defn- face->edges
  "Converts a face (set of three points) into a sequence of edge lines. Each
  edge will be sorted such that the 'lower' point comes first in the vector."
  [face]
  (let [points (seq face)]
    (->>
      points
      (cons (last points))
      (partition 2 1)
      (map (comp vec sort)))))



;;;;; GEODESIC DEFINITION ;;;;;

(def icosahedron
  "The set of faces making up an icosahedron with unit radius. Each face
  is a set of three coordinate vectors."
  (let [-phi (- phi)
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
    (map-shape points faces)))


(defn edges
  "Calculates a seq of edges forming a geodesic sphere with the given radius
  and tesselation number."
  [r n]
  (->> icosahedron
       (map-shape (partial cartesian/rotate-x phi-angle))
       (split-faces n)
       (map-shape (partial cartesian/project-to r))
       (mapcat face->edges)))


(defn ground-slice
  "Cuts off the bottom of a sphere by removing edges which extend below the
  horizontal plane."
  [edges]
  (remove
    (fn [[[_ _ z1] [_ _ z2]]]
      (or (neg? z1) (neg? z2)))
    edges))
