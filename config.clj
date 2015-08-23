;; System configuration


(def dome-radius
  "Inside radius of the geodesic dome frame in meters. Approximately 12.1 feet."
  3.688)

(def barrel-radius
  "Outside radius of the barrel lantern in meters. Approximately 4 inches."
  0.099)

(def pixel-strip
  "Strip definition with pixel count and inter-pixel spacing."
  {:spacing 0.02  ; 2 cm
   :pixels 240})


(defconfig :layout
  (layout/join
    (layout/group :lantern
      (layout/translate
        [0 0 (- dome-radius 0.1)]
        (layout/barrel barrel-radius 0.068 2 pixel-strip)))
    #_
    (layout/group :dome
      (layout/geodesic-grid
        dome-radius
        :pixel-spacing (:spacing pixel-strip)
        :strut-pixels
        [50 62 64 64]
        :strip-struts
        [[0 6 15 8]
         [2 14 22 16]
         [4 18 20 23]
         [3 10 12 19]
         [1 5 7 11]]))))


(defconfig :event-handler
  (-> state/update-mode
      (handler/mode-selector)
      (handler/toggle-autocycle)
      (handler/autocycle-modes
        (comp #{:button/press :button/repeat} :type)
        :period 60)
      (handler/control-code :konami
        :code [:up :up :down :down :left :right :left :right :B :A :start]
        :mode :strobe)
      (handler/control-code :strobe-shortcut
        :code [:X :X :X :X :X :Y]
        :mode :strobe)
      (handler/control-code :lantern
        :code [:L :R :L :R :X :Y]
        :mode :lantern)
      (handler/control-code :bombs
        :code [:A :A :A :A :X :Y]
        :mode :bombs)
      (handler/buffer-keys 20)
      (handler/log-events (comp #{:button/press} :type))
      (handler/system-reset)))


(defconfig :web-options
  {:port 8080
   :min-threads 2
   :max-threads 5
   :max-queued 25})



;; Inputs

(definput :timer
  timer/timer
  (async/chan (async/dropping-buffer 3))
  33)


(definput :audio
  audio/audio-input
  (async/chan (async/sliding-buffer 10))
  100)


(definput :gamepad
  gamepad/snes
  (async/chan (async/dropping-buffer 10)))



;; Modes

(defmode ant 2.0 4)

(defmode beachball
  [(color/rgb 1 1 1)
   (color/rgb 1 0 0)
   (color/rgb 1 1 1)
   (color/rgb 1 1 0)
   (color/rgb 1 1 1)
   (color/rgb 0 0 1)])

(defmode bombs)

(defmode dart)

(defmode flicker 5 240)

(defmode lantern 0.5)

(defmode pulse (color/rgb 1 0 0))

(defmode rainbow)

(defmode ring)

(defmode strip-eq)

(defmode strobe
  [(color/rgb 1 0 0)
   (color/rgb 0 1 0)
   (color/rgb 0 0 1)])

(defmode tunes)


(defconfig :playlist
  [:flicker
   :dart
   :ant
   :beachball
   :rainbow])
