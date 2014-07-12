(ns user
  (:require
    [clojure.core.async :as async :refer [<! <!! >! >!!]]
    [clojure.java.io :as io]
    [clojure.pprint :refer [pprint]]
    [clojure.repl :refer :all]
    [clojure.string :as str]
    [clojure.tools.namespace.repl :refer [refresh]]
    [com.stuartsierra.component :as component]
    (org.playasophy.wonderdome
      [core :as wonder]
      [state :as state]
      [util :refer [color]])
    (org.playasophy.wonderdome.display
      [core :as display]
      [processing :as processing])
    (org.playasophy.wonderdome.input
      [middleware :as middleware])
    (org.playasophy.wonderdome.mode
      [strobe :refer [strobe]])
    [org.playasophy.wonderdome.geometry.layout :as layout]))


(def dimensions
  "Geodesic dome and pixel strip dimensions."
  {:radius 3.688         ; 12.1'
   :pixel-spacing 0.02   ; 2 cm
   :strip-pixels 240
   :strips 6})


; TODO: dynamically load modes?
(def config
  {:layout (layout/star dimensions)
   :display (processing/display [1000 600] (:radius dimensions))
   :timer-ms 1000
   :handler (-> state/update-mode
                (middleware/print-events (comp #{} :type)))
   :modes
   {:strobe (strobe [(color 255 0 0) (color 0 255 0) (color 0 0 255)])}})


(def system
  (wonder/initialize config))


(defn start!
  "Initialize the wonderdome for local development."
  []
  (alter-var-root #'system component/start)
  (display/set-colors!
    (:display system)
    (repeat 6 (repeat 240 (color 255 255 0))))
  :started)


(defn stop!
  "Stops the wonderdome system and closes the display window."
  []
  (alter-var-root #'system component/stop)
  :stopped)


(defn reload!
  []
  (stop!)
  (refresh :after 'user/start!))
