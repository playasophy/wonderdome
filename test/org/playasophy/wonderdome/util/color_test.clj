(ns org.playasophy.wonderdome.util.color-test
  (:require
    [clojure.test :refer :all]
    [org.playasophy.wonderdome.util.color :as color]
    [quil.core :as quil]))


;;;;; COLOR HARNESS ;;;;;

(defn- setup-harness
  []
  (quil/frame-rate 5)
  (quil/text-font (quil/create-font "Courier" 18 true))
  ;(quil/blend-mode :blend)
  (quil/background 0)
  (quil/stroke 0))


(defn- render-harness
  []
  (let [red   (color/rgb 1 0 0)  ; hue 0/3
        green (color/rgb 0 1 0)  ; hue 1/3
        blue  (color/rgb 0 0 1)] ; hue 2/3
    (quil/background 0)
    (quil/stroke 0)

    ; red, green, and blue blocks
    (quil/with-translation [20 20]
      (quil/with-translation [0 0]
        (quil/fill (color/gray 0.8))
        (quil/text "Primary colors" 0 0)
        (quil/fill red)
        (quil/rect 0 10 40 20)
        (quil/fill (color/hsv 0/3 1.0 1.0))
        (quil/rect 0 30 40 20)
        (quil/fill green)
        (quil/rect 50 10 40 20)
        (quil/fill (color/hsv 1/3 1.0 1.0))
        (quil/rect 50 30 40 20)
        (quil/fill (color/rgb 0 0 1))
        (quil/rect 100 10 40 20)
        (quil/fill (color/hsv 2/3 1.0 1.0))
        (quil/rect 100 30 40 20))

      ; RGB blending
      (quil/with-translation [170 0]
        (quil/fill (color/gray 0.8))
        (quil/text "RGB blending" 0 0)
        (quil/fill (color/blend-rgb 0.5 red green))
        (quil/rect 0 10 40 40)
        (quil/fill (color/blend-rgb 0.5 green blue))
        (quil/rect 50 10 40 40)
        (quil/fill (color/blend-rgb 0.5 blue red))
        (quil/rect 100 10 40 40))

      ; HSV blending
      (quil/with-translation [340 0]
        (quil/fill (color/gray 0.8))
        (quil/text "HSV blending" 0 0)
        (quil/fill (color/blend-hsv 0.5 red green))
        (quil/rect 0 10 40 40)
        (quil/fill (color/blend-hsv 0.5 green blue))
        (quil/rect 50 10 40 40)
        (quil/fill (color/blend-hsv 0.5 blue red))
        (quil/rect 100 10 40 40))))

    ; HSV spectrum
    (quil/with-translation [20 100]
      (quil/fill (color/gray 0.8))
      (quil/text "HSV Spectrum" 0 0)
      (let [width 480, height 100]
        (quil/fill 0)
        (quil/stroke (color/gray 0.75))
        (quil/rect -1 9 (inc width) (inc height))
        (doseq [x (range 0 width)
                y (range 0 height)]
          (quil/stroke (color/hsv (/ x width) 1 (- 1 (/ y height))))
          (quil/point x (+ y 10)))))

    ; HSL spectrum
    (quil/with-translation [20 240]
      (quil/fill (color/gray 0.8))
      (quil/text "HSL Spectrum" 0 0)
      (let [width 480, height 100]
        (quil/fill 0)
        (quil/stroke (color/gray 0.75))
        (quil/rect -1 9 (inc width) (inc height))
        (doseq [x (range 0 width)
                y (range 0 height)]
          (quil/stroke (color/hsl (/ x width) 1 (- 1 (/ y height))))
          (quil/point x (+ y 10)))))

    ; Cubehelix rainbow
    (quil/with-translation [20 380]
      (quil/fill (color/gray 0.8))
      (quil/text "Cubehelix rainbow" 0 0)
      (let [half-width 240
            c0 (color/hsl* (/ -100 360) 0.75 0.35)
            c1 (color/hsl* (/   80 360) 1.50 0.80)
            c2 (color/hsl* (/  260 360) 0.75 0.35)]
        (doseq [x (range 0 240)]
          (let [p (/ x half-width)]
            (quil/stroke (color/blend-cubehelix p c0 c1))
            (quil/line x 10 x 50)
            (quil/stroke (color/blend-cubehelix p c1 c2))
            (quil/line (+ x half-width) 10 (+ x half-width) 50))))))


(defn color-harness
  "Creates and starts a sketch to demonstrate the color utility functions."
  []
  (let []
    (quil/sketch
      :title "Wonderdome Color Harness"
      :setup setup-harness
      :draw render-harness
      :size [520 460])))
