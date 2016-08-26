(ns playasophy.wonderdome.mode.tunes
  (:require
    [playasophy.wonderdome.geometry.sphere :as sphere :refer [pi tau]]
    [playasophy.wonderdome.mode.core :as mode]
    [playasophy.wonderdome.util.color :as color]
    [playasophy.wonderdome.util.control :as ctl]))


;; General idea:
;; - Divide up the circle into `n` slices, each assigned to a part of the
;; frequency spectrum.
;; - Assign a rainbow spectrum to each of the slices.
;; - As time passes, rotate the angular offset of slices in physical space.
;; - As time passes, rotate the offset into the rainbow for each slice.
;; - Maintain a rolling average of the power in a particular frequency. Power
;; decay is controlled by `decay` parameter.
;; - Adjust the gain per segment based on recently observed history to try to
;; keep values normalized.
;;
;; For a given slice, pixels along the slice will be colored based on the
;; rainbow offset. The current power in the slice determines how far the color
;; should extend away from the polar axis. More energy = further coloring.
;;
;; To color a particular pixel, figure out which two slices it falls between,
;; average the color and intensity. Should be a quick lookup ideally.
;;
;; As a stretch goal, if beats are detected, color the first few pixels of the
;; strips (up to a max) a totally different color which fades back to zero.

(def power-factor 9/10)
(def power-decay 1/200)
(def power-target 2.0)


(defrecord FrequencyBand
  [gain    ; Multiplier for the energy input to this band
   energy  ; Smoothed recent energy levels after applying gain
   power   ; Power output integral over a longer time period (no gain)
   ])


(defn- apply-decay
  "Updats a frequency band to indicate time has passed and decayed the value."
  [band elapsed decay]
  (-> band
      (update :energy * (ctl/bound [0.0 1.0] (- 1.0 (* 1/1000 decay elapsed))))
      (update :power  * (ctl/bound [0.0 1.0] (- 1.0 (* power-decay elapsed))))
      (assoc :gain (ctl/bound [0.2 5.0]
                              (cond
                                ; If power is lower than we want, add gain
                                (< (/ (:power band) power-target) 0.8)
                                  (* (:gain band) 1.01)
                                ; If power is higher than we want, back off gain
                                (> (/ (:power band) power-target) 1.2)
                                  (* (:gain band) 0.99)
                                :else
                                  (:gain band))))))


(defn- update-energy
  "Updates a frequency band with a new input energy sample."
  [band energy smoothing]
  (assoc band
         :energy (+ (* smoothing (or (:energy band) 0.0))
                    (* (- 1 smoothing)
                       (or (:gain band) 0.0)
                       (or energy 0.0)))
         :power (+ (* power-factor (or (:power band) 0.0))
                   (* (- 1 power-factor) (or energy 0.0)))))


(defn- azimuth->bands
  "Given an azimuthal angle (around the pole), determine the two bands the
  angle falls between. Returns a vector containing the index of the first and
  second bands and the proportion between the two."
  [n angle]
  {:pre [(pos? n)]}
  (let [band-width (/ tau n)
        angle' (if (neg? angle)
                 (+ angle tau)
                 angle)
        angle-div (/ angle' band-width)
        low-band (int (Math/floor angle-div))
        high-band (int (Math/ceil angle-div))]
    [low-band
     (if (<= n high-band) 0 high-band)
     (- angle-div low-band)]))


(defn- dt-update
  "Updates the given value by applying the rate (in seconds) multiplied by the
  elapsed time (in milliseconds)."
  [value rate elapsed]
  (+ value (* 1/1000 elapsed rate)))


(defrecord TunesMode
  [bands          ; Average energy per band
   decay          ; How quickly energy levels decay (frac/s)
   smoothing      ; Proportion to smooth input samples by (fraction of old average to keep)
   rotation       ; How much to rotate each band spatially
   rotation-rate  ; How fast the rotation changes over time
   color-shift    ; How much to shift the colors
   shift-rate     ; How fast the color shifts over time
   log-next?      ; Debugging helper state
   ]

  mode/Mode

  (update
    [this event]
    (case [(:type event) (:input event)]
      [:time/tick nil]
      (let [elapsed (or (:elapsed event) 0.0)]
        (cond->
          (assoc this
               :bands (mapv #(apply-decay % elapsed decay) bands)
               :rotation (sphere/wrap-angle (dt-update rotation rotation-rate elapsed))
               :color-shift (ctl/wrap [0.0 1.0] (dt-update color-shift shift-rate elapsed)))
          (compare-and-set! log-next? true false)
            (doto prn)))

      [:audio/beat nil]
      ; TODO: something
      this

      [:audio/freq nil]
      (assoc this
             :bands (mapv #(update-energy %1 %2 smoothing)
                          bands (:spectrum event)))

      [:button/press :L]
      (do (reset! log-next? true)
          this)

      [:button/press :A]
      (update this :decay #(ctl/bound [0.1 10.0] (* 1.1 %)))

      [:button/press :B]
      (update this :decay #(ctl/bound [0.1 10.0] (* 0.9 %)))

      [:axis/direction :x-axis]
      (update this :rotation-rate
              ctl/adjust event
              :rate 0.2
              :min-val -2.0
              :max-val 2.0)

      [:axis/direction :y-axis]
      (update this :shift-rate
              ctl/adjust event
              :rate 0.2
              :min-val -5.0
              :max-val 5.0)

      ; default
      this))


  (render
    [this pixel]
    (let [[_ polar azimuth] (:sphere pixel)
          [i1 i2 p] (azimuth->bands (count bands) (+ azimuth rotation))
          hue-index (/ (+ i1 p) (count bands))
          energy (+ (* (:energy (nth bands i1)) (- 1 p))
                    (* (:energy (nth bands i2)) p))
          polar-max (* 1/5 tau)
          transition-point (* polar-max (- 1 (Math/exp (- energy))))
          transition-width (* 1/16 tau)
          brightness (cond
                       (< polar (- transition-point transition-width))
                         1.0
                       (> polar (+ transition-point transition-width))
                         0.0
                       :else
                         (- 1.0 (/ (- polar (- transition-point transition-width))
                                   transition-width)))]
      (color/hsv
        (ctl/wrap [0.0 1.0] (+ hue-index color-shift))
        1
        brightness))))


(defn init
  "Creates a new sound-reactive mode with `n` frequency bands."
  [n & {:as opts}]
  (map->TunesMode
    (merge
      {:decay 5.0
       :smoothing 0.3
       :rotation-rate 0.5
       :shift-rate 0.0}
      opts
      {:bands (vec (take n (repeatedly #(map->FrequencyBand {:gain 1.0
                                                             :energy 0.0
                                                             :power 0.0}))))
       :rotation 0.0
       :color-shift 0.0
       :log-next? (atom false)})))
