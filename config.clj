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
    #_
    (layout/group :lantern
      (layout/translate
        [0 0 (- dome-radius 0.1)]
        (layout/barrel barrel-radius 0.068 2 pixel-strip)))
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
      (handler/toggle-autocycle
        :code [:R :R :R :R :start])
      (handler/autocycle-modes
        (comp #{:button/press :button/repeat} :type)
        :period 60)
      (handler/control-code :konami
        :code [:up :up :down :down :left :right :left :right :B :A :start]
        :mode :strobe)
      (handler/control-code :strobe-shortcut
        :code [:X :X :X :X :X :Y :start]
        :mode :strobe)
      (handler/control-code :lantern
        :code [:L :R :L :R :X :Y :start]
        :mode :lantern)
      (handler/control-code :bombs
        :code [:A :A :A :A :X :Y :start]
        :mode :bombs)
      (handler/buffer-keys 20)
      #_(handler/log-events (comp #{:audio/beat :button/press} :type))
      (handler/system-reset)))


(defconfig :web-options
  {:port 8080
   :min-threads 3
   :max-threads 10
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

(defmode :ant 6.0 7)

(def beachball-colors
  [color/white color/red color/white color/yellow color/white color/blue])
(def american-colors
  [color/blue color/blue color/blue color/white color/red color/white color/red color/white color/red color/white])
(def rainbow-colors
  (map color/rainbow (range 0 1 0.1)))
(def fire-colors
  (let [colors [(color/rgb 156/256 42/256 34/256) (color/rgb 226/256 87/256 34/256) (color/rgb 1.0 73/256 73/256) (color/rgb 1.0 189/256 91/256) (color/rgb 253/256 207/256 88/256)]]
    (repeatedly 12 #(rand-nth colors))))

(defmode :beachball
  [beachball-colors american-colors rainbow-colors fire-colors])

(defmode :bombs)

(defmode :dart)

(defmode :flicker 5 240)

(defmode :lantern 0.5)

(defmode :pulse (color/rgb 1 0 0))

(defmode :rainbow)

(defmode :ring)

(defmode :strip-eq)

(defmode :strobe
  [(color/rgb 1 0 0)
   (color/rgb 0 1 0)
   (color/rgb 0 0 1)])

(defmode :tunes 27)


(defconfig :playlist
  [:beachball
   :dart
   :tunes
   :flicker
   :rainbow
   :strip-eq
   :ant])
