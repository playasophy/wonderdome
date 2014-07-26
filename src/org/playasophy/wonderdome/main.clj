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


;;;;; CONFIGURATION ;;;;;

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



;;;;; LIFECYCLE ;;;;;

(def system nil)


(defn- start!
  "Constructs an initialized Wonderdome system."
  [config-path]
  (->
    (load-config-file
      config-path
      '[org.playasophy.wonderdome.state :as state]
      '[org.playasophy.wonderdome.display.pixel-pusher :refer [pixel-pusher]]
      '[org.playasophy.wonderdome.geometry.layout :as layout]
      '[org.playasophy.wonderdome.input.middleware :as middleware])

    ; TODO: load some kind of saved state?
    ; TODO: move modes to config file
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


(defn- stop!
  "Halts the running wonderdome system."
  []
  (log/info "Stopping Wonderdome system...")
  (when system
    (component/stop system)))



;;;;; ENTRY POINT ;;;;;

(defn -main [& [config-path]]
  (alter-var-root #'system (constantly (start! config-path)))
  (.addShutdownHook
    (Runtime/getRuntime)
    (Thread. stop! "Wonderdome Shutdown Hook")))
