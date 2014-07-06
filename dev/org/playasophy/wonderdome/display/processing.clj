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
  220.0)


(defn- setup-sketch
  []
  (quil/background 255)
  (quil/stroke 0))


(defn- draw-axes
  [length]
  (let [l (* scale length)]
    ; x-axis red
    (quil/stroke (quil/color 255 0 0))
    (quil/line [0 0 0] [l 0 0])
    ; y-axis green
    (quil/stroke (quil/color 0 255 0))
    (quil/line [0 0 0] [0 l 0])
    ; z-axis blue
    (quil/stroke (quil/color 0 0 255))
    (quil/line [0 0 0] [0 0 l])))


(defn- draw-ground
  [radius]
  (let [c (quil/color 210 200 175)
        r (* scale radius)]
    (quil/fill c)
    (quil/stroke c)
    (quil/ellipse 0 0 r r)))


(defn- draw-dome
  [radius]
  (quil/stroke (quil/color 0))
  (doseq [[a b] (geodesic/dome-struts radius 3)]
    (apply quil/line
      (concat (map (partial * scale) a)
              (map (partial * scale) b)))))


(defn- render
  [display]
  (quil/background 255)
  (quil/translate (* 0.50 (quil/width)) (* 0.55 (quil/height)) 0)
  (quil/rotate-x 1.2)
  (quil/rotate-z (* (quil/frame-count) 0.003))
  (draw-axes 1.0)
  (draw-ground 4.0)
  (draw-dome (:dome-radius display))
  ; TODO: render pixels
  )



;;;;; WONDERDOME DISPLAY ;;;;;

(defrecord ProcessingDisplay
  [size dome-radius layout pixels])

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
    (when-let [sketch (:applet this)]
      (applet/applet-close sketch))
    (dissoc this :applet))


  display/Display

  (set-pixels!
    [this pixels]
    (swap! (:pixels this) (constantly pixels))
    this))


(defn display
  "Creates a new simulation display using Processing. Takes a vector giving the
  width and height in pixels, and a radius of geometric dome to draw."
  [size radius]
  (ProcessingDisplay.
    size radius nil
    (atom [] :validator vector?)))
