(ns org.playasophy.wonderdome.input.gamepad-harness
  (:require
    [clojure.core.async :as async :refer [<!]]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.input.gamepad :as gamepad]))


(def system nil)


(defn init!
  "Initializes the gamepad harness."
  []
  (when-not system
    (alter-var-root #'system
      (constantly
        (let [channel (async/chan (async/dropping-buffer 5))]
          (if-let [input (gamepad/snes channel)]
            (component/system-map
              :channel channel
              :input input
              :process
              (async/go-loop []
                (prn (<! channel))
                (recur)))
            (println "No USB input device found!")))))))


(defn start!
  "Creates and starts a gamepad input component attached to a channel and reader
  which will print events received to the console."
  []
  (when-not system
    (init!))
  (alter-var-root #'system component/start)
  :start)


(defn stop!
  "Stops the gamepad harness from reporting events."
  []
  (when system
    (alter-var-root #'system component/stop))
  :stop)
