(ns org.playasophy.wonderdome.display.clone
  (:require
    [org.playasophy.wonderdome.display.core :as display]))


(defrecord CloneDisplay
  []

  display/Display

  (set-colors!
    [this colors]
    (dorun (map #(display/set-colors! % colors) (vals this)))
    nil))


(defn clone
  "Creates a new display which broadcasts colors to any displays associated with
  it as keys in the record."
  [& displays]
  (apply assoc (CloneDisplay.) displays))
