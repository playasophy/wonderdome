(ns org.playasophy.wonderdome.mode.config
  "Collected mode configurations."
  (:require
    (org.playasophy.wonderdome.mode
      [lantern :refer [lantern]]
      [rainbow :refer [rainbow]]
      [strobe :refer [strobe]])
    (org.playasophy.wonderdome.util
      [color :as color])))


(def config
  "Map of mode configuration."
  {:rainbow
   (rainbow)

   :strobe
   (strobe [(color/rgb 1 0 0)
            (color/rgb 0 1 0)
            (color/rgb 0 0 1)])

   :lantern
   (lantern 0.5)})


(def playlist
  "Initial mode playlist."
  [:rainbow
   :strobe
   :lantern])
