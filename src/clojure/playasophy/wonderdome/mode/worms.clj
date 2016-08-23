(ns playasophy.wonderdome.mode.worms
  (:require
    [clojure.tools.logging :as log]
    [playasophy.wonderdome.geometry.sphere :as sphere]
    [playasophy.wonderdome.mode.core :as mode]
    [playasophy.wonderdome.util.color :as color]
    [playasophy.wonderdome.util.control :as control]))


(defrecord WormsMode
  [worm]

  mode/Mode

  (update
    [this event]
    (condp = [(:type event) (:input event)]
      [:time/tick nil]
      (let [elapsed (or (:elapsed event) 0.0)]
        (update-in this [:worm :age] #(+ elapsed %)))

      this))


  (render
    [this pixel]
    (do
      #_(log/warn pixel bombs)
      color/yellow)))


(defn init
  "Creates a new worms mode with starting parameters"
  ([]
   (init {:from_node 0 :to_node 1 :age 0}))
  ([worm]
   (WormsMode. worm)))
