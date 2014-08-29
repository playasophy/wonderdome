(ns org.playasophy.wonderdome.util.control
  (:require
    [org.playasophy.wonderdome.util.color :as color]))



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
