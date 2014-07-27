(ns org.playasophy.wonderdome.main
  "Main entry-point for launching the Wonderdome code in production."
  (:gen-class)
  (:require
    [clojure.core.async :as async]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    (org.playasophy.wonderdome
      [config :as config]
      [state :as state]
      [system :as system])
    [org.playasophy.wonderdome.display.pixel-pusher :as pixel-pusher]
    (org.playasophy.wonderdome.input
      [gamepad :as gamepad]
      [timer :as timer])))


; Force java.util.logging through SLF4J/Logback
(org.slf4j.bridge.SLF4JBridgeHandler/removeHandlersForRootLogger)
(org.slf4j.bridge.SLF4JBridgeHandler/install)



;;;;; LIFECYCLE ;;;;;

(def system nil)


(defn- start!
  "Constructs an initialized Wonderdome system."
  [config-path]
  (->
    (config/load config-path)

    (assoc :display (pixel-pusher/display))

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
