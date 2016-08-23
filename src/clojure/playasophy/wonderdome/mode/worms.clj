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

(def edges-by-strip
  [[[:v1 :v2 50] [:v2 :v6 62] [:v6 :v11 64] [:v11 :v2 64]]
   [[:v1 :v3 50] [:v3 :v2 62] [:v2 :v7 64] [:v7 :v3 64]]
   [[:v1 :v4 50] [:v4 :v3 62] [:v3 :v8 64] [:v8 :v4 64]]
   [[:v1 :v5 50] [:v5 :v4 62] [:v4 :v9 64] [:v9 :v5 64]]
   [[:v1 :v6 50] [:v6 :v5 62] [:v5 :v10 64] [:v10 :v6 64]]])

(def edges
  [[:v1 :v2] [0 0 50]
   [:v2 :v6] [0 50 112]
   [:v6 :v11] [0 112 176]
   [:v11 :v2] [0 176 240]
   [:v1 :v3] [1 0 50]
   [:v3 :v2] [1 50 112]
   [:v2 :v7] [1 112 176]
   [:v7 :v3] [1 176 240]
   [:v1 :v4] [2 0 50]
   [:v4 :v3] [2 50 112]
   [:v3 :v8] [2 112 176]
   [:v8 :v4] [2 176 240]
   [:v1 :v5] [3 0 50]
   [:v5 :v4] [3 50 112]
   [:v4 :v9] [3 112 176]
   [:v9 :v5] [3 176 240]
   [:v1 :v6] [4 0 50]
   [:v6 :v5] [4 50 112]
   [:v5 :v10] [4 112 176]
   [:v10 :v6] [4 176 240]])

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
