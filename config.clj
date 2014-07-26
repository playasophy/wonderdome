{:layout
 (layout/star
   {:radius 3.688         ; 12.1'
    :pixel-spacing 0.02   ; 2 cm
    :strip-pixels 240
    :strips 6})

 :display
 (pixel-pusher)

 :handler
 (-> state/update-mode
     middleware/mode-selector
     (middleware/autocycle-modes (comp #{:button/press :button/repeat} :type)))

 :web-options
 {:port 8080
  :min-threads 3
  :max-threads 10
  :max-queued 25}}
