(ns playasophy.wonderdome.util.control
  (:require
    [playasophy.wonderdome.util.color :as color]))



(defn adjust
  [v event & {:keys [rate min-val max-val]
                   :or {rate 1.0
                        min-val 0.0
                        max-val 1.0}}]
  (let [delta (* (or (:value event) 0)
                 (or (:elapsed event) 0)
                 1/1000
                 rate)]
    (-> v (+ delta) (min max-val) (max min-val))))

(defn adjust-wrapped
  [v event & {:keys [rate min-val max-val]
                   :or {rate 1.0
                        min-val 0.0
                        max-val 1.0}}]
  (let [delta (* (or (:value event) 0)
                 (or (:elapsed event) 0)
                 1/1000
                 rate)
        normalized-v (- v min-val)
        normalized-v' (+ normalized-v delta)
        modulus (- max-val min-val)
        wrapped-v' (mod normalized-v' modulus)
        v' (+ wrapped-v' min-val)]
    v'))
