(ns playasophy.wonderdome.mode.ring
  (:require
    [clojure.tools.logging :as log]
    [playasophy.wonderdome.mode.core :as mode]
    [playasophy.wonderdome.util.color :as color]))


(defrecord RingMode
  [ring-base ring-width]

  mode/Mode

  (update
    [this event]
    (condp = [(:type event) (:input event)]
      [:button/press :L]
      (do
        (log/warn (pr-str event))
        this)

      this))


  (render
    [this pixel]
    (let [pixel-z (-> pixel :barrel :normalized-z)]
      (if (and (not (nil? pixel-z)) (>= pixel-z ring-base) (<= pixel-z (+ ring-base ring-width)))
        (color/gray 1.0)
        color/none))))


(defn init
  "Creates a new ring mode with starting parameters"
  ([]
   (init 0.4 0.2))
  ([ring-base ring-width]
   (RingMode. ring-base ring-width)))
