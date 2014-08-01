(ns org.playasophy.wonderdome.input.gamepad-harness
  (:require
    [clojure.core.async :as async :refer [<!]]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.input.gamepad :as gamepad]))


(def system nil)


(defn start!
  "Creates and starts a gamepad input component attached to a channel and reader
  which will print events received to the console."
  []
  (if system
    (alter-var-root #'system
      update-in [:input] component/start)
    (alter-var-root #'system
      (constantly
        (let [channel (async/chan (async/dropping-buffer 5))]
          (if-let [input (gamepad/snes channel)]
            {:input (component/start input)
             :channel channel
             :process (async/go-loop []
                        (prn (<! channel))
                        (recur))}
            (println "No USB input device found!"))))))
  :start)


(defn stop!
  "Stops the wonderdome system and closes the display window."
  []
  (when system
    (alter-var-root #'system update-in [:input] component/stop))
  :stop)
