(ns playasophy.wonderdome.mode.strip_eq
  (:require
    [playasophy.wonderdome.geometry.sphere :as sphere]
    [playasophy.wonderdome.mode.core :as mode]
    [playasophy.wonderdome.util.color :as color]
  )
)

; StripEQ will process audio signals and turn each strip into an indicator for
; the strength of a range of frequencies over time.
(defrecord StripEqMode
  [num_strips pix_per_strip]

  mode/Mode

  (update
    [this event]
    (case (:type event)
      :time/tick
      (assoc this
             ; TODO: Vary colorindex over time smoothly.
             :colorindex (double (/ 1.0 num_strips))
      )

      :audio/beat
      (assoc this
             ; TODO: Reserve one strip for beats?
             :beat/at (System/currentTimeMillis)
             :beat/power (:power event)
      )

      :audio/freq
      (assoc this
             :freq/at (System/currentTimeMillis)
             ; Break frequencies into groups per strip and take mean for each.
             :freq/group_mean (map
               #(/ (reduce + %) (count %))
               (partition
                 (int (/ (count (:spectrum event)) num_strips))
                 (:spectrum event)
               )
             )
      )

      ; default
      this
    )
  )


  (render
    [this pixel]
    (let [
      ; Frequency for this strip from most recent signal.
      ; Assumes frequencies range from 0 to 1.
      freq (nth (:freq/group_mean this) (:strip pixel))
      ; How far down the strip is this pixel?
      ; Ranges from 0 (start of strip) to 1 (end of strip).
      pix_to_len_pct (double (/ (inc (:pixel pixel)) (inc pix_per_strip)))
      ]
      (if (< pix_to_len_pct (or freq 0.0))
        ; Each strip has the same color,
        ; but strips form an even spread over the color spectrum.
        (color/rainbow (* (:strip pixel) (:colorindex this)))
        color/none
      )
    )
  )
)

(defn strip_eq
  "Creates a new StripEQ visualizer mode."
  []
  (StripEqMode. 5 256)
)
