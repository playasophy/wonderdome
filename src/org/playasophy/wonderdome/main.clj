(ns org.playasophy.wonderdome.main
  "Main entry-point for launching the Wonderdome code in production."
  (:gen-class)
  (:require
    [clojure.core.async :as async]
    [com.stuartsierra.component :as component]
    (org.playasophy.wonderdome
      [state :as state]
      [system :as system])
    (org.playasophy.wonderdome.display
      [pixel-pusher :refer [pixel-pusher]])
    (org.playasophy.wonderdome.geometry
      [layout :as layout])
    (org.playasophy.wonderdome.input
      [gamepad :as gamepad]
      [middleware :as middleware]
      [timer :as timer])
    [org.playasophy.wonderdome.mode.config :as modes]))


(def dimensions
  "Geodesic dome and pixel strip dimensions."
  {:radius 3.688         ; 12.1'
   :pixel-spacing 0.02   ; 2 cm
   :strip-pixels 240
   :strips 6})


(defn -main [& args]
  (let [{:keys [timer-period]
         :or {timer-period 30}}
        (map read-string args)]
    (->
      {:layout
       (layout/star dimensions)

       :display
       (pixel-pusher)

       :handler
       (-> state/update-mode
           middleware/mode-selector
           (middleware/autocycle-modes (comp #{:button/press :button/repeat} :type)))

       ; TODO: load some kind of saved state
       :initial-state
       (state/initialize modes/config)}

      system/initialize

      (system/add-input :timer timer/timer
        (async/chan (async/dropping-buffer 3))
        timer-period)

      (system/add-input :gamepad gamepad/snes
        (async/chan (async/dropping-buffer 10)))

      ; TODO: audio parser

      component/start)))
