(ns playasophy.wonderdome.mode.config
  "Collected mode configurations."
  (:require
    (playasophy.wonderdome.mode
      ant
      flicker
      lantern
      pulse
      rainbow
      strobe
      dart 
      tunes)
    potemkin))


(potemkin/import-vars
  (playasophy.wonderdome.mode.ant ant)
  (playasophy.wonderdome.mode.flicker flicker)
  (playasophy.wonderdome.mode.lantern lantern)
  (playasophy.wonderdome.mode.pulse pulse)
  (playasophy.wonderdome.mode.rainbow rainbow)
  (playasophy.wonderdome.mode.strobe strobe)
  (playasophy.wonderdome.mode.dart dart)
  (playasophy.wonderdome.mode.tunes tunes))
