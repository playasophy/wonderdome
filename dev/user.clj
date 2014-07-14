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
      [state :as state]
      [system :as system])
    (org.playasophy.wonderdome.display
      [processing :as processing])
    (org.playasophy.wonderdome.geometry
      [layout :as layout])
    (org.playasophy.wonderdome.input
      [middleware :as middleware]
      [timer :refer [timer]])
    (org.playasophy.wonderdome.mode
      [core :refer [color]]
      [strobe :refer [strobe]])))


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
   :handler (-> state/update-mode
                #_ (middleware/print-events (comp #{:dt} :type)))
   :modes
   {:strobe (strobe [(color 255 0 0)
                     (color 0 255 0)
                     (color 0 0 255)])}})


(def system
  (-> config
      system/initialize
      (system/add-input :timer timer
        (async/chan (async/dropping-buffer 3))
        100)
      ; TODO: usb input
      ; TODO: audio parser
      ))


(defn start!
  "Initialize the wonderdome for local development."
  []
  (alter-var-root #'system component/start)
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
