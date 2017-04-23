;              11
;             /   \
;            /     \
;           6-------2
;          /|\     /|\
;         / |  \  / | \
;       10  |   1   |  7
;         \ |  /|\  | /
;          \|/  |  \|/
;           5---4---3
;           |  / \  |
;           |/     \|
;           9       8


(ns playasophy.wonderdome.mode.worms
  (:require
    [clojure.tools.logging :as log]
    [playasophy.wonderdome.geometry.sphere :as sphere]
    [playasophy.wonderdome.mode.core :as mode]
    [playasophy.wonderdome.util.color :as color]
    [playasophy.wonderdome.util.control :as control]))

(defrecord Edge
  [from_node to_node strip ]
  )


(def edges-lookup (apply hash-map edges))


(defn- is-on
  [pixel edge]
  (let [pixel-offset (pixel :pixel)
        pixel-strip (pixel :strip)
        [edge-strip start end] (edges-lookup edge)]
    (and
      (= pixel-strip edge-strip)
      (>= pixel-offset start)
      (< pixel-offset end))))

(defrecord WormsMode
  [edge log-next dummy]

  mode/Mode

  (update
    [this event]
    (condp = [(:type event) (:input event)]
      [:time/tick nil]
      (let [elapsed (or (:elapsed event) 0.0)]
        ; Just flip a bit back and forth to force rendering.
        (update this :dummy not))

      [:button/press :A]
      (do
        (swap! log-next not)
        this)

      [:button/press :B]
      ; TODO: Fix the edges array so that we don't have to do this shady "(+ 2 %)" thing.
      (update this :edge #(mod (+ 2 %) (count edges)))

      this))


  (render
    [this pixel]
    (do
      (if (compare-and-set! log-next true false)
        (log/warn edge (edges-lookup (edges edge))))
      (if (is-on pixel (edges edge))
        color/yellow
        color/none))))


(defn init
  "Creates a new worms mode with starting parameters"
  ([]
   (init 0))
  ([edge]
   (WormsMode. edge (atom false) false)))
