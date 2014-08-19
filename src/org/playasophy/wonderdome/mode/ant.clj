(ns org.playasophy.wonderdome.mode.ant
  (:require
    [org.playasophy.wonderdome.mode.core :as mode]
    [org.playasophy.wonderdome.util.color :as color]))


(def ^:private ^:const adjustment-rate
  "Rate at which the brightness level changes per millisecond."
  0.0005)


(defrecord AntMode
  [position speed length]

  mode/Mode

  (update
    [this event]
    (condp = [(:type event) (:button event)]
      [:time/tick nil]
      (let [elapsed (or (:elapsed event) 0.0)
            position' (+ position (* (/ elapsed 1000) speed))
            position' (if (>= position' 1200) 0 position')]
        (assoc this :position position'))

      [:button/press :L]
      (assoc this :speed 1)

      [:button/press :R]
      (assoc this :speed 10)

      [:button/press :A]
      (assoc this :length 1)

      [:button/press :B]
      (assoc this :length 10)

      #_[:button/repeat :y-axis]
      #_(let [delta (* (or (:value event) 0)
                     (or (:elapsed event) 0)
                     adjustment-rate)
            level (-> brightness (+ delta) (min 1.0) (max 0.0))]
        (assoc this :brightness level))

      this))


  (render
    [this pixel]
    (if (< (rem (+ position (:pixel pixel)) (* 2 length)) length)
      (color/gray 1.0)
      (color/gray 0.0))))


(defn ant
  "Creates a new ant mode with starting speed, and length."
  [speed, length]
  (AntMode. 0 speed length))
