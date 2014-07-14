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


(def modes
  "Map of mode values."
  ; TODO: dynamically load modes?
  {:strobe (strobe [(color 255 0 0) (color 0 255 0) (color 0 0 255)])})


(def system nil)


(defn init!
  "Initialize the wonderdome system for local development."
  []
  (alter-var-root #'system
    (constantly
      (->
        {:layout (layout/star dimensions)
         :display (processing/display [1000 600] (:radius dimensions))
         :handler (-> state/update-mode
                      #_ (middleware/print-events (comp #{:dt} :type)))
         :modes modes}
        system/initialize
        (system/add-input :timer timer
          (async/chan (async/dropping-buffer 3))
          100)
        ; TODO: usb input
        ; TODO: audio parser
        )))
  :init)


(defn start!
  "Starts the wonderdome system running."
  []
  (when system
    (alter-var-root #'system component/start))
  :start)


(defn go!
  "Initializes and starts the wonderdome system."
  []
  (init!)
  (start!))


(defn stop!
  "Stops the wonderdome system and closes the display window."
  []
  (when system
    (alter-var-root #'system component/stop))
  :stop)


(defn reload!
  "Reloads all changed namespaces to update code, then re-launches the system."
  []
  (stop!)
  (refresh :after 'user/go!))
