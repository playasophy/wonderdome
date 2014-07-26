(ns org.playasophy.wonderdome.main
  "Main entry-point for launching the Wonderdome code in production."
  (:gen-class)
  (:require
    [clojure.core.async :as async]
    [clojure.java.io :as io]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    (org.playasophy.wonderdome
      [state :as state]
      [system :as system])
    (org.playasophy.wonderdome.input
      [gamepad :as gamepad]
      [timer :as timer])
    [org.playasophy.wonderdome.mode.config :as modes]))


; Force java.util.logging through SLF4J/Logback
(org.slf4j.bridge.SLF4JBridgeHandler/removeHandlersForRootLogger)
(org.slf4j.bridge.SLF4JBridgeHandler/install)


(defn- load-config-file
  "Reads a Clojure configuration file. The given require forms will be loaded
  and made available to the configuration."
  [path & requirements]
  (let [file (io/file path)]
    (when (.exists file)
      (let [temp-ns (gensym)]
        (try
          (binding [*ns* (create-ns temp-ns)]
            (clojure.core/refer-clojure)
            (when (seq requirements)
              (apply require requirements))
            (load-string (slurp path)))
          (catch Exception e
            (log/error e (str "Error loading config file: " path))
            nil)
          (finally (remove-ns temp-ns)))))))


(def dimensions
  "Geodesic dome and pixel strip dimensions."
  {:radius 3.688         ; 12.1'
   :pixel-spacing 0.02   ; 2 cm
   :strip-pixels 240
   :strips 6})


(defn -main [& [config-path]]
  (->
    (load-config-file
      config-path
      '[org.playasophy.wonderdome.state :as state]
      '[org.playasophy.wonderdome.display.pixel-pusher :refer [pixel-pusher]]
      '[org.playasophy.wonderdome.geometry.layout :as layout]
      '[org.playasophy.wonderdome.input.middleware :as middleware])

    ; TODO: load some kind of saved state
    (assoc :initial-state
      (state/initialize modes/config))

    system/initialize

    (system/add-input :timer timer/timer
      (async/chan (async/dropping-buffer 3))
      30)

    (system/add-input :gamepad gamepad/snes
      (async/chan (async/dropping-buffer 10)))

    ; TODO: audio parser

    component/start))
