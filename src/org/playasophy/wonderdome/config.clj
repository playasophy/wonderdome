(ns org.playasophy.wonderdome.config
  "Functions for loading system configuration."
  (:refer-clojure :exclude [load])
  (:require
    [clojure.java.io :as io]
    [clojure.tools.logging :as log]))


(defn- load-with-requires
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
            (def defsystem hash-map) ; FIXME
            (load-string (slurp path)))
          (catch Exception e
            (log/error e (str "Error loading config file: " path))
            (throw e))
          (finally (remove-ns temp-ns)))))))


(defn load
  "Load a system configuration file."
  [path]
  (load-with-requires
    path
    '[org.playasophy.wonderdome.geometry.layout :as layout]
    '[org.playasophy.wonderdome.handler :as handler]
    '[org.playasophy.wonderdome.mode.config :as mode]
    '[org.playasophy.wonderdome.state :as state]
    '[org.playasophy.wonderdome.util.color :as color]))
