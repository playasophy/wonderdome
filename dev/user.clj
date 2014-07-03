(ns user
  (:require
    [clj-time.core :as time]
    [clojure.java.io :as io]
    [clojure.pprint :refer [pprint]]
    [clojure.repl :refer :all]
    [clojure.string :as str]
    [clojure.tools.namespace.repl :refer [refresh]]
    [environ.core :refer [env]]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.core :as wonder]
    [org.playasophy.wonderdome.display.sketch :as sketch]
    [org.playasophy.wonderdome.layout.radial :as radial]))


(def dimensions
  "Geodesic dome and pixel strip dimensions."
  {:radius 3.688         ; 12.1'
   :pixel-spacing 0.02   ; 2 cm
   :strip-pixels 240
   :strips 6})


(def config
  {:layout (radial/layout dimensions)
   :display (sketch/processing-display 800 450)
   :modes []})


(def system
  (wonder/initialize config))


(defn start!
  "Initialize the wonderdome for local development."
  []
  (alter-var-root #'system component/start))


(defn stop!
  "Stops the wonderdome system and closes the display window."
  []
  (alter-var-root #'system component/stop))


(defn reload!
  []
  (stop!)
  (refresh :after 'user/start!))
