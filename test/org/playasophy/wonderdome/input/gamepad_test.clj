(ns org.playasophy.wonderdome.input.gamepad-test
  (:require
    [clojure.core.async :as async :refer [<!]]
    [clojure.test :refer :all]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.input.gamepad :as gamepad]))


(defn gamepad-test
  "Creates and starts a gamepad input component attached to a channel and reader
  which will print events received to the console."
  []
  (let [channel (async/chan (async/dropping-buffer 5))
        input (gamepad/snes channel)]
    (async/go-loop []
      (prn (<! channel))
      (recur))
    (component/start input)))
