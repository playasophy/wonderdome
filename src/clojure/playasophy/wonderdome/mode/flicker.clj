(ns playasophy.wonderdome.mode.flicker
  (:require
    [playasophy.wonderdome.mode.core :as mode]
    [playasophy.wonderdome.util.color :as color]
    [playasophy.wonderdome.util.control :as control]))


(def ^:const ^:private hue-range 0.137)
(def ^:const ^:private hue-delta 0.05)

(def ^:const ^:private brightness-rate 0.05)

; TODO: 'speed' is more like 'delay'
(def ^:const ^:private min-speed 5)
(def ^:const ^:private max-speed 3500)
(def ^:const ^:private speed-rate -200)


(defn- retarget-pixel
  "Picks a new color and time-to-target for a pixel with the given initial
  state. Pixels have a starting color, a target color, a time to transition
  from one to the other, and how much transition has happened so far."
  [{:keys [speed hue max-brightness]} pixel]
  {:start (:target pixel)
   :target (color/hsv
             (+ hue (rand hue-range))
             (rand)
             (rand max-brightness))
   :duration (+ (rand-int speed) min-speed)
   :elapsed 0})


(defn- update-pixel
  "Updates a pixel with a certain amount of elapsed time."
  [config elapsed pixel]
  (let [pixel' (update-in pixel [:elapsed] + elapsed)]
    (if (> (:elapsed pixel') (:duration pixel'))
      (retarget-pixel config pixel')
      pixel')))


(defrecord FlickerMode
  [speed max-brightness hue pixels]

  mode/Mode

  (update
    [this event]
    (case [(:type event) (:input event)]
      [:time/tick nil]
      (assoc this :pixels
        (vec (map #(vec (map (partial update-pixel this (:elapsed event)) %))
                  pixels)))

      [:axis/direction :x-axis]
      (assoc this :speed (control/adjust speed event :rate speed-rate :min-val min-speed :max-val max-speed))

      [:axis/direction :y-axis]
      (assoc this :max-brightness (control/adjust max-brightness event :rate brightness-rate))

      [:button/press :L]
      (assoc this :speed max-speed)

      [:button/press :R]
      (assoc this :speed min-speed)

      [:button/press :A]
      (assoc this :hue (control/wrap [0.0 1.0] (- hue hue-delta)))

      [:button/press :B]
      (assoc this :hue (control/wrap [0.0 1.0] (+ hue hue-delta)))

      this))


  (render
    [this p]
    (let [{:keys [start target duration elapsed]} (nth (nth pixels (:strip p)) (:pixel p)) ]
      (color/blend-hsv (/ elapsed duration) start target))))


(defn init
  "Creates a new flicker mode with the given color sequence."
  [strips strip-pixels]
  (let [config (FlickerMode. 1500 1.0 0.137 nil)
        fresh-pixel #(retarget-pixel config {:target 0})
        fresh-strip #(vec (repeatedly strip-pixels fresh-pixel))]
    (assoc config :pixels
      (vec (repeatedly strips fresh-strip)))))
