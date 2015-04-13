(ns playasophy.wonderdome.display.processing
  (:require
    [clojure.core.async :as async :refer [>!!]]
    [com.stuartsierra.component :as component]
    [playasophy.wonderdome.display.core :as display]
    (playasophy.wonderdome.geometry
      [geodesic :as geodesic])
    [playasophy.wonderdome.util.quil :refer [*scale-factor* scale-point draw-axes]]
    (quil
      [applet :as applet]
      [core :as quil])))


;;;;; RENDERING FUNCTIONS ;;;;;

(defn- setup-sketch
  []
  (quil/background 0)
  (quil/stroke 0))


(defn- draw-dome
  [edges]
  (quil/stroke (quil/color 96 128))
  (quil/stroke-weight 3)
  (doseq [[a b] edges]
    (quil/line
      (scale-point a)
      (scale-point b))))


(defn- draw-strip
  "Draws a line representing the path of a pixel strip. Strip should be a vector
  of spherical coordinate maps."
  [strip]
  (quil/stroke-weight 1)
  (quil/stroke 0 64 196)
  (doseq [[a b] (partition 2 1 strip)]
    (quil/line
      (-> a :coord scale-point)
      (-> b :coord scale-point))))


(defn- draw-pixel
  "Draws a single pixel with the given color. The pixel should be a spherical
  coordinate map."
  [coordinate color]
  (quil/stroke-weight 3)
  (when-not (zero? (quil/brightness color))
    (quil/stroke color)
    (->> coordinate
         :coord
         scale-point
         (apply quil/point))))


(defn- draw-pixel-strips
  [layout colors]
  (dorun (map draw-strip layout))
  (dorun (map #(dorun (map draw-pixel %1 %2)) layout colors)))


(defn- render
  [display]
  (quil/background 0)
  (quil/translate (* 1/2 (quil/width)) (* 3/4 (quil/height)) 0)
  (quil/rotate-x 2.3)
  (quil/rotate-z (* (quil/frame-count) 0.003))
  (binding [*scale-factor* 1.5]
    (draw-axes 0.5)
    ;(draw-ground 4.0)
    (draw-dome (:dome display))
    (draw-pixel-strips
      (:layout display)
      @(:colors display))))



;;;;; INPUT FUNCTIONS ;;;;;

(def ^:private key-codes
  "Maps integer key codes to keywords."
  {10 :start    ; enter
   32 :select   ; space
   37 :left
   38 :up
   39 :right
   40 :down
   65 :A
   66 :B
   76 :L
   82 :R
   88 :X
   89 :Y})


(def ^:private axis-keys
  #{:up :down :left :right})


(defn- key->event
  "Generates button press and repeat events for appropriate key actions."
  [old-key new-key dt]
  (when new-key
    (if (= new-key old-key)
      (if (< dt 80)
        ; Repeated key-press events
        (case new-key
          :left  {:type :button/repeat, :button :x-axis, :value -1.0, :elapsed dt}
          :right {:type :button/repeat, :button :x-axis, :value  1.0, :elapsed dt}
          :down  {:type :button/repeat, :button :y-axis, :value -1.0, :elapsed dt}
          :up    {:type :button/repeat, :button :y-axis, :value  1.0, :elapsed dt}
          nil)
        ; New press of same key
        (when-not (axis-keys new-key)
          {:type :button/press, :button new-key}))
      ; New key pressed
      (when-not (axis-keys new-key)
        {:type :button/press, :button new-key}))))


(defn- key-handler
  "Builds a handler function which will store the current key state in an atom
  and report gamepad-compatible events to the given channel."
  [channel]
  (let [state (atom {})]
    (fn []
      (when (quil/key-pressed?)
        (let [code (quil/key-code)
              new-key (key-codes code)
              now (System/currentTimeMillis)
              {:keys [old-key last-press]} @state
              dt (if last-press (- now last-press) 0)]
          (when-let [event (key->event old-key new-key dt)]
            (>!! channel event))
          ; TODO: need timers to handle button/release events
          (swap! state assoc :old-key new-key :last-press now))))))



;;;;; PROCESSING DISPLAY ;;;;;

(defrecord ProcessingDisplay
  [size dome layout colors event-channel]

  component/Lifecycle

  (start
    [this]
    (assoc this :sketch
      (quil/sketch
        :title "Playasophy Wonderdome"
        :features [:keep-on-top :resizable]
        :setup setup-sketch
        :draw #(render this)
        :size (:size this)
        :key-pressed (key-handler event-channel)
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
  width and height in pixels, and a radius of geometric dome to draw. The pixel
  layout must be injected at runtime before starting the display."
  [size radius]
  (let [dome (-> radius (+ 0.05) (geodesic/edges 3) geodesic/ground-slice set)]
    (ProcessingDisplay. size dome nil (atom []) nil)))
