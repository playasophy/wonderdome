(ns playasophy.wonderdome.util.control
  (:require
    [playasophy.wonderdome.util.color :as color]))



(defn bound
  [[lower-bound upper-bound] value]
  (let [normalized-value (- value lower-bound)
        modulus (- upper-bound lower-bound)
        wrapped-value (mod normalized-value modulus)]
    (+ wrapped-value lower-bound)))

(defn wrap
  [[lower-bound upper-bound] value]
  (-> value (max lower-bound) (min upper-bound)))

(defn adjust
  [v event & {:keys [rate min-val max-val]
                   :or {rate 1.0
                        min-val 0.0
                        max-val 1.0}}]
  (let [delta (* (or (:value event) 0)
                 (or (:elapsed event) 0)
                 1/1000
                 rate)]
    (bound [min-val max-val] (+ v delta))))

(defn adjust-wrapped
  [v event & {:keys [rate min-val max-val]
                   :or {rate 1.0
                        min-val 0.0
                        max-val 1.0}}]
  (let [delta (* (or (:value event) 0)
                 (or (:elapsed event) 0)
                 1/1000
                 rate)]
    (wrap [min-val max-val] (+ v delta))))
