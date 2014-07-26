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
      [config :as config]
      [state :as state]
      [system :as system])
    (org.playasophy.wonderdome.display
      [pixel-pusher :refer [pixel-pusher]]
      [processing :as processing])
    (org.playasophy.wonderdome.geometry
      [layout :as layout])
    (org.playasophy.wonderdome.input
      [gamepad :as gamepad]
      [middleware :as middleware]
      [timer :as timer])))


(def dome-radius
  "Geodesic dome radius in meters (~12.1')."
  3.688)


(def system nil)


(defn init!
  "Initialize the wonderdome system for local development."
  [config-path]
  (alter-var-root #'system
    (constantly
      (->
        (config/load config-path)

        (assoc :display
          (component/using
            (processing/display [1000 600] dome-radius)
            [:layout :event-channel]))

        system/initialize

        (system/add-input :timer timer/timer
          (async/chan (async/dropping-buffer 3))
          30)

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
  "Initializes with the default config and starts the wonderdome system."
  []
  (init! "config.clj")
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
