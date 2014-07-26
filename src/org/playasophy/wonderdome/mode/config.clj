(ns org.playasophy.wonderdome.mode.config
  "Collected mode configurations."
  (:require
    (org.playasophy.wonderdome.mode
      lantern
      rainbow
      strobe)
    (org.playasophy.wonderdome.util
      [color :as color])
    [potemkin :refer [import-vars]]))


(import-vars
  (org.playasophy.wonderdome.mode.lantern lantern)
  (org.playasophy.wonderdome.mode.rainbow rainbow)
  (org.playasophy.wonderdome.mode.strobe strobe))
