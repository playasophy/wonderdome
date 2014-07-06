(ns org.playasophy.wonderdome.layout.geodesic)


;;;;; CONSTANTS ;;;;;

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


(def phi
  "The golden ratio."
  (/ (+ 1 (Math/sqrt 5.0)) 2))


(def phi-angle
  "Angle formed by a 1 x phi rectangle."
  (Math/asin (/ (Math/sqrt (+ 1 (* phi phi))))))



;;;;; POINT MANIPULATION ;;;;;

(defn- map-shape
  "Takes a shape (a set of faces, each of which is a set of points, each of
  which is a vector of 3 coordinates, and applies a function to each point."
  [f shape]
  (set (map (comp set (partial map f)) shape)))


(defn- rotate-x
  "Rotates a point around the X axis."
  [theta [x y z]]
  (let [cos (Math/cos theta)
        sin (Math/sin theta)]
    [x
     (- (* y cos) (* z sin))
     (+ (* y sin) (* z cos))]))


(defn- project-radius
  "Projects a point to the same vector on the surface of the unit sphere."
  [r p]
  (let [m (Math/sqrt (apply + (map #(* % %) p)))]
    (vec (map #(/ % m) p))))



;;;;; SHAPE FUNCTIONS ;;;;;

(defn- midpoint
  "Returns a new point that is a fraction of the distance towards point b
  from point a."
  [p a b]
  (->>
    (map - b a)
    (map (partial * p))
    (map + a)))


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
        (let [d (midpoint 1/2 a b)
              e (midpoint 1/2 b c)
              f (midpoint 1/2 c a)]
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
            (mapcat (partial split-face (/ n 3)))
            set))

        :else
        (throw (IllegalArgumentException.
                 (str n " is not divisible by any supported prime")))))))


(defn- split-faces
  [n shape]
  (set (mapcat (partial split-face n) shape)))


(defn- face->edges
  "Converts a face (set of three points) into a sequence of edge lines."
  [face]
  (as-> face points
    (seq points)
    (cons (last points) points)
    (partition 2 1 points)))



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


(defn dome-struts
  [r n]
  (->> icosahedron
       (map-shape (partial rotate-x phi-angle))
       (split-faces n)
       (map-shape (partial project-radius r))
       (mapcat face->edges)
       (remove (fn [[[_ _ z1] [_ _ z2]]] (or (neg? z1) (neg? z2))))))
