(ns org.playasophy.wonderdome.util)


(defn gradient
  "Assigns a color based on the position through a gradient of color points."
  [blend-color colors value]
  (let [s  (count colors)
        t  (* (- value (int value)) s)
        t0 (int t)
        t1 (if (>= (inc t0) s) 0 (inc t0))
        p  (- t t0)]
    (blend-color (colors t0) (colors t1) p)))
