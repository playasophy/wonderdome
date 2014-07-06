(ns org.playasophy.wonderdome.display.processing
  (:require
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.display :as display]
    [org.playasophy.wonderdome.layout.geodesic :as geodesic]
    (quil
      [applet :as applet]
      [core :as quil])
    [quil.helpers.drawing :refer [line-join-points]]))


;;;;; PROCESSING SKETCH ;;;;;

(def scale
  "Scale up all coordinates to map meters to screen space."
  60.0)


(defn- setup-sketch
  []
  (quil/background 255)
  (quil/stroke 0))


(defn- draw-axes
  [length]
  ; x-axis red
  (quil/stroke (quil/color 255 0 0))
  (quil/line [0 0 0] [length 0 0])
  ; y-axis green
  (quil/stroke (quil/color 0 255 0))
  (quil/line [0 0 0] [0 length 0])
  ; z-axis blue
  (quil/stroke (quil/color 0 0 255))
  (quil/line [0 0 0] [0 0 length]))


(defn- draw-ground
  [radius]
  (let [c (quil/color 210 200 175)]
    (quil/fill c)
    (quil/stroke c)
    (quil/ellipse 0 0 radius radius)))


(defn- draw-dome
  [radius]
  (quil/stroke (quil/color 0))
  (doseq [face geodesic/dome-faces]
    (-> face
        (as-> points
          ; scale the points
          (map #(vec (map (partial * scale display) %)) points)
          (vec points)
          ; repeat the last point
          (conj points (first points)))
        line-join-points
        (as-> lines
          (map (partial apply quil/line) lines))
        dorun)))


(defn- render
  [display]
  (quil/background 255)
  (quil/translate (* 0.50 (quil/width)) (* 0.55 (quil/height)) 0)
  (quil/rotate-x 1.2)
  #_(quil/rotate-y (* (quil/frame-count) 0.003))
  (draw-axes 150)
  (draw-ground (* scale 20))
  (draw-dome (:radius display))
  ; TODO: render pixels
  )



;;;;; WONDERDOME DISPLAY ;;;;;

(defrecord ProcessingDisplay
  [size radius layout pixels])

(extend-type ProcessingDisplay
  component/Lifecycle

  (start
    [this]
    (assoc this :applet
      (quil/sketch
        :title "Playasophy Wonderdome"
        :setup setup-sketch
        :draw #(render this)
        :size (:size this)
        :renderer :opengl)))


  (stop
    [this]
    (applet/applet-close (:applet this))
    (dissoc this :applet))


  display/Display

  (set-pixels!
    [this pixels]
    (swap! (:pixels this) (constantly pixels))
    this))


(defn display
  "Creates a new simulation display using Processing. Takes a width and height
  in pixels."
  [width height radius]
  (ProcessingDisplay.
    [width height] radius nil
    (atom [] :validator vector?)))
