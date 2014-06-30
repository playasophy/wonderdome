(ns org.playasophy.wonderdome.display.sketch
  (:require
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.display :as display]
    [quil.core :as q]))


(defrecord ProcessingDisplay
  [])

(extend-type ProcessingDisplay
  component/Lifecycle

  (start
    [this]
    (println "Starting Processing display...")
    this)

  (stop
    [this]
    (println "Stopping Processing display...")
    this)

  display/Display

  (set-pixel!
    [this pixel color]
    nil))
