(ns org.playasophy.wonderdome.display.processing
  (:require
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.display :as display]
    [org.playasophy.wonderdome.geodesic :as geodesic]
    (quil
      [applet :as applet]
      [core :as quil])))


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
    (quil/stroke-weight 1)
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
  [edges]
  (quil/stroke (quil/color 96 128))
  (quil/stroke-weight 3)
  (doseq [[a b] edges]
    (apply quil/line
      (concat (map (partial * scale) a)
              (map (partial * scale) b)))))


(defn- render
  [display]
  (quil/background 0)
  (quil/translate (* 0.50 (quil/width)) (* 0.55 (quil/height)) 0)
  (quil/rotate-x 1.2)
  (quil/rotate-z (* (quil/frame-count) 0.003))
  (draw-axes 0.5)
  ;(draw-ground 4.0)
  (draw-dome (:dome display))
  ; TODO: render pixels
  )



;;;;; WONDERDOME DISPLAY ;;;;;

(defrecord ProcessingDisplay
  [size dome layout colors])

(extend-type ProcessingDisplay
  component/Lifecycle

  (start
    [this]
    (assoc this :sketch
      (quil/sketch
        :title "Playasophy Wonderdome"
        :setup setup-sketch
        :draw #(render this)
        :size (:size this)
        :renderer :opengl)))


  (stop
    [this]
    (when-let [sketch (:sketch this)]
      (applet/applet-close sketch))
    (dissoc this :sketch))


  display/Display

  (set-colors!
    [this colors]
    (swap! (:colors this) (constantly colors))
    nil))


(defn display
  "Creates a new simulation display using Processing. Takes a vector giving the
  width and height in pixels, and a radius of geometric dome to draw."
  [size radius]
  (let [dome (-> radius (geodesic/edges 3) geodesic/slice set)]
    (ProcessingDisplay.
      size dome nil
      (atom [] :validator vector?))))
