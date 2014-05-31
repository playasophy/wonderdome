(ns org.playasophy.wonderdome.geodesic
  (:require
    [quil.core :refer :all]
    [quil.helpers.drawing :refer [line-join-points]]
    [quil.helpers.calc :refer [mul-add]]))


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


; chord-factor n = 2*sin(theta/2)
; theta = central angle
; length of chord = r*n

; 0.3486 = 1*2*sin(t/2)
; 0.3486/2 = sin(t/2)
; arcsin(0.3486/2) = t/2
; 2*arcsin(ratio/2)


(def icosahedron
  "An icosahedron with unit radius."
  (let [phi (/ (+ 1 (Math/sqrt 5.0)) 2)
        -phi (- phi)
        points
        {:A [ 0.0  1.0  phi] :B [ 0.0 -1.0  phi]
         :C [ 0.0 -1.0 -phi] :D [ 0.0  1.0 -phi]
         :E [ phi  0.0  1.0] :F [-phi  0.0  1.0]
         :G [-phi  0.0 -1.0] :H [ phi  0.0 -1.0]
         :I [ 1.0  phi  0.0] :J [-1.0  phi  0.0]
         :K [-1.0 -phi  0.0] :L [ 1.0 -phi  0.0]}
        faces
        [[:A :I :J] [:A :J :F] [:A :F :B] [:A :B :E] [:A :E :I]
         [:B :F :K] [:B :K :L] [:B :L :E] [:C :D :H] [:C :H :L]
         [:C :L :K] [:C :K :G] [:C :G :D] [:D :G :J] [:D :J :I]
         [:D :I :H] [:E :L :H] [:E :H :I] [:F :J :G] [:F :G :K]]]
    (set (map (comp set (partial map points)) faces))))


;;;;; SKETCH CODE ;;;;;

(defn setup []
  (background 255)
  (stroke 00))


(defn draw []
  (background 255)
  (translate (/ (width) 2) (/ (height) 2) 0)
  (rotate-y (* (frame-count) 0.03))
  (rotate-x (* (frame-count) 0.04))
  (doseq [face icosahedron]
    (-> face
        (as-> points
          (map #(vec (map (partial * 50) %)) points)   ; scale the points
          (vec points)
          (conj points (first points)))                ; repeat the last point
        line-join-points
        (as-> lines
          (map (partial apply line) lines))
        dorun))
  #_
  (let [line-args (for [t (range 0 180)]
                    (let [s (* t 18)
                          radian-s (radians s)
                          radian-t (radians t)
                          x (* radius (cos radian-s) (sin radian-t))
                          y (* radius (sin radian-s) (sin radian-t))
                          z (* radius (cos radian-t))]
                      [x y z]))]
    (dorun
     (map #(apply line %) (line-join-points line-args)))))


(defsketch geodesic-dome
  :title "Geodesic Dome"
  :setup setup
  :draw draw
  :size [500 300]
  :renderer :opengl)
