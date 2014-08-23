(ns org.playasophy.wonderdome.mode.flicker
  (:require
    [org.playasophy.wonderdome.mode.core :as mode]
    [org.playasophy.wonderdome.util.color :as color]))


(def ^:const ^:private hue-range 0.137)
(def ^:const ^:private hue-delta 0.05)

(def ^:const ^:private brightness-rate 0.027)

; TODO: 'speed' is more like 'delay'
(def ^:const ^:private min-speed 10)
(def ^:const ^:private max-speed 3500)
(def ^:const ^:private speed-rate 100)


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
    (case [(:type event) (:button event)]
      [:time/tick nil]
      (assoc this :pixels
        (vec (map #(vec (map (partial update-pixel this (:elapsed event)) %))
                  pixels)))

      [:button/repeat :x-axis]
      (let [delta (* (or (:value event) 0)
                     (or (:elapsed event) 0)
                     speed-rate)
            speed' (-> speed (+ delta) (min max-speed) (max 0))]
        (assoc this :speed speed'))

      [:button/repeat :y-axis]
      (let [delta (* (or (:value event) 0)
                     (or (:elapsed event) 0)
                     brightness-rate)
            max-brightness' (-> max-brightness (+ delta) (min 1.0) (max 0.0))]
        (assoc this :max-brightness max-brightness'))

      [:button/press :A]
      (assoc this :hue
        (let [hue' (- hue hue-delta)]
          (if (neg? hue')
            (+ hue 1.0)
            hue')))

      [:button/press :B]
      (assoc this :hue
        (let [hue' (+ hue hue-delta)]
          (if (> hue' 1.0)
            (- hue' 1.0)
            hue')))

      this))


  (render
    [this p]
    (let [{:keys [start target duration elapsed]} (nth (nth pixels (:strip p)) (:pixel p)) ]
      (color/blend-hsv (/ elapsed duration) start target))))


(defn flicker
  "Creates a new flicker mode with the given color sequence."
  [strips strip-pixels]
  (let [config (FlickerMode. 1500 1.0 0.137 nil)
        fresh-pixel #(retarget-pixel config {:target 0})
        fresh-strip #(vec (repeatedly strip-pixels fresh-pixel))]
    (assoc config :pixels
      (vec (repeatedly strips fresh-strip)))))
