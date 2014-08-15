(ns org.playasophy.wonderdome.main
  "Main entry-point for launching the Wonderdome code in production."
  (:gen-class)
  (:require
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    (org.playasophy.wonderdome
      [config :as config]
      [system :as system])
    (org.playasophy.wonderdome.display
      [pixel-pusher :as pixel-pusher])))


; Force java.util.logging through SLF4J/Logback
(org.slf4j.bridge.SLF4JBridgeHandler/removeHandlersForRootLogger)
(org.slf4j.bridge.SLF4JBridgeHandler/install)



;;;;; LIFECYCLE ;;;;;

(def system nil)


(defn- start!
  "Constructs an initialized Wonderdome system."
  [config-path]
  (log/info "Starting Wonderdome system...")
  (->
    (config/load config-path)
    (assoc :display (pixel-pusher/display))
    system/initialize
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
    (Thread. ^Runnable stop! "Wonderdome Shutdown Hook"))
  (log/info "System started, entering active mode..."))
