;; System configuration

(defconfig :layout
  (layout/geodesic-grid
    :radius 3.688
    :pixel-spacing 0.02
    :strut-pixels
    [50 62 64 64]
    :strip-struts
    [[0 6 15 8]
     [2 14 22 16]
     [4 18 20 23]
     [3 10 12 19]
     [1 5 7 11]])

  #_
  (layout/star
    :radius 3.688        ; 12.1'
    :pixel-spacing 0.02  ; 2 cm
    :strip-pixels 240
    :strips 6))


(defconfig :event-handler
  (-> state/update-mode
      (handler/mode-selector)
      (handler/autocycle-modes
        (comp #{:button/press :button/repeat} :type)
        :period 90)
      (handler/control-code :konami
        :code [:up :up :down :down :left :right :left :right :B :A :start]
        :mode :strobe)
      (handler/buffer-keys 20)
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

(defmode dart)

(defmode flicker 5 240)

(defmode lantern 0.5)

(defmode pulse (color/rgb 1 0 0))

(defmode rainbow)

(defmode strip-eq)

(defmode strobe
  [(color/rgb 1 0 0)
   (color/rgb 0 1 0)
   (color/rgb 0 0 1)])

(defmode tunes)


(defconfig :playlist
  [:flicker
   :strip-eq
   :dart
   :rainbow
   :ant])
