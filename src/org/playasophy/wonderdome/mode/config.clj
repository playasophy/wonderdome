(ns org.playasophy.wonderdome.mode.config
  "Collected mode configurations."
  (:require
    (org.playasophy.wonderdome.mode
      ant
      lantern
      pulse
      rainbow
      strobe
      tunes)
    potemkin))


(potemkin/import-vars
  (org.playasophy.wonderdome.mode.ant ant)
  (org.playasophy.wonderdome.mode.lantern lantern)
  (org.playasophy.wonderdome.mode.pulse pulse)
  (org.playasophy.wonderdome.mode.rainbow rainbow)
  (org.playasophy.wonderdome.mode.strobe strobe)
  (org.playasophy.wonderdome.mode.tunes tunes))
