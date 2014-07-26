(ns org.playasophy.wonderdome.mode.config
  "Collected mode configurations."
  (:require
    (org.playasophy.wonderdome.mode
      lantern
      rainbow
      strobe)
    potemkin))


(potemkin/import-vars
  (org.playasophy.wonderdome.mode.lantern lantern)
  (org.playasophy.wonderdome.mode.rainbow rainbow)
  (org.playasophy.wonderdome.mode.strobe strobe))
