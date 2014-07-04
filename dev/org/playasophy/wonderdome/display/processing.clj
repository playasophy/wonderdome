(ns org.playasophy.wonderdome.display.processing
  (:require
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.display :as display]
    (quil
      [applet :as applet]
      [core :as quil])))


;;;;; PROCESSING SKETCH ;;;;;

(defn- setup-sketch
  []
  (quil/background 255)
  (quil/stroke 0))


(defn- draw-pixels
  [display]
  (quil/background 255)
  (quil/translate (/ (quil/width) 2) (/ (quil/height) 2) 0)
  (quil/rotate-y (* (quil/frame-count) 0.003))
  (quil/rotate-x (* (quil/frame-count) 0.004))
  (quil/stroke (quil/color 255 0 0)) (quil/line [0 0 0] [150 0 0])  ; x-axis red
  (quil/stroke (quil/color 0 255 0)) (quil/line [0 0 0] [0 150 0])  ; y-axis green
  (quil/stroke (quil/color 0 0 255)) (quil/line [0 0 0] [0 0 150])  ; z-axis blue
  (quil/stroke (quil/color 0))
  ; TODO: draw geodesic dome outline
  ; TODO: render pixels
  )



;;;;; WONDERDOME DISPLAY ;;;;;

(defrecord ProcessingDisplay
  [size layout pixels])

(extend-type ProcessingDisplay
  component/Lifecycle

  (start
    [this]
    (assoc this :applet
      (quil/sketch
        :title "Playasophy Wonderdome"
        :setup setup-sketch
        :draw #(draw-pixels this)
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
  [width height]
  (ProcessingDisplay.
    [width height] nil
    (atom [] :validator vector?)))
