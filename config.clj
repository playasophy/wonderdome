(hash-map

  :layout
  #_
  (layout/geodesic
    :radius 3.688
    :pixel-spacing 0.02
    :strut-pixels
    [48 64 64 64]
    :strip-struts
    [[0 6 15 8]
     [2 14 22 16]
     [4 18 20 23]
     [3 10 12 19]
     [1 5 7 11]])

  (layout/star
    :radius 3.688         ; 12.1'
    :pixel-spacing 0.02   ; 2 cm
    :strip-pixels 240
    :strips 6)

  :event-handler
  (-> state/update-mode
      handler/mode-selector
      (handler/autocycle-modes
        (comp #{:button/press :button/repeat} :type)))

  :web-options
  {:port 8080
   :min-threads 2
   :max-threads 5
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
