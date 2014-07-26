(hash-map

  :layout
  (layout/star
    {:radius 3.688         ; 12.1'
     :pixel-spacing 0.02   ; 2 cm
     :strip-pixels 240
     :strips 6})

  :event-handler
  (-> state/update-mode
      handler/mode-selector
      (handler/autocycle-modes
        (comp #{:button/press :button/repeat} :type)))

  :web-options
  {:port 8080
   :min-threads 3
   :max-threads 10
   :max-queued 25}

  :modes
  {:rainbow
   (mode/rainbow)

   :strobe
   (mode/strobe
     [(color/rgb 1 0 0)
      (color/rgb 0 1 0)
      (color/rgb 0 0 1)])

   :lantern
   (mode/lantern 0.5)}

  :playlist
  [:rainbow
   :strobe
   :lantern])
