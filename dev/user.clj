(ns user
  (:require
    [clojure.core.async :as async :refer [<! <!! >! >!!]]
    [clojure.java.io :as io]
    [clojure.pprint :refer [pprint]]
    [clojure.repl :refer :all]
    [clojure.stacktrace :refer [print-cause-trace]]
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
      [gamepad :as gamepad]
      [middleware :as middleware]
      [timer :as timer])
    (org.playasophy.wonderdome.mode
      [lantern :refer [lantern]]
      [strobe :refer [strobe]])
    (org.playasophy.wonderdome.util
      [color :as color])))


(def dimensions
  "Geodesic dome and pixel strip dimensions."
  {:radius 3.688         ; 12.1'
   :pixel-spacing 0.02   ; 2 cm
   :strip-pixels 240
   :strips 6})


(def modes
  "Map of mode values."
  ; TODO: dynamically load modes?
  {:lantern (lantern 0.5)
   :strobe (strobe [(color/rgb 1 0 0) (color/rgb 0 1 0) (color/rgb 0 0 1)])})


(def system nil)


(defn init!
  "Initialize the wonderdome system for local development. Accepts an optional
  key-value sequence to set options:

  :timer-period     milliseconds per timer tick"
  [& {:keys [timer-period]
      :or {timer-period 30}}]
  (alter-var-root #'system
    (constantly
      (->
        {:layout (layout/star dimensions)
         :display (processing/display [1000 600] (:radius dimensions))
         :handler (-> state/update-mode
                      (middleware/print-events (comp #{} :type)))
         :state (state/initialize modes)}
        system/initialize
        (system/add-input :timer timer/timer
          (async/chan (async/dropping-buffer 3))
          timer-period)
        (system/add-input :gamepad gamepad/snes
          (async/chan (async/dropping-buffer 10)))
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
