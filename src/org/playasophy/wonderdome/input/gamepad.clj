(ns org.playasophy.wonderdome.input.gamepad
  "A gamepad input watches for button presses from a USB game controller."
  (:require
    [clojure.tools.logging :as log]
    [org.playasophy.wonderdome.input.usb-hid :as usb]))


;;;;; HELPER FUNCTIONS ;;;;;

(defn- byte-axis
  "Converts a byte value from [00, FF] into a floating-point axis value from
  [-1.0, 1.0]."
  [value]
  (->
    (cond
      (zero? value) 0
      (neg? value) (+ 0x101 value)
      :else (inc value))
    (/ 128.0)
    (- 1.0)))


(defn- read-buttons
  "Takes in a definition map from button keys to a vector of [byte bit-pos] and
  a buffer, and returns a map of button keys to booleans."
  [buttons ^bytes buffer]
  (->>
    buttons
    (map (fn [[k [b n]]]
           [k (bit-test (aget buffer b) n)]))
    (into {})))


(defn- button-events
  "Calculates button press and release events. Returns a seq of events."
  [buttons old-state new-state]
  (for [[button default] buttons]
    (let [old-val (get old-state button)
          new-val (get new-state button)]
      (when (not= old-val new-val)
        (if (number? new-val)
          (cond
            (= default new-val)
            {:type :button/release, :source :gamepad, :button button, :value new-val}
            (= default old-val)
            {:type :button/press, :source :gamepad, :button button, :value new-val})
          (cond
            new-val
            {:type :button/press, :button button, :source :gamepad}
            old-val
            {:type :button/release, :button button, :source :gamepad}))))))


(defn- repeat-events
  "Calculates repeated button 'hold' events, where the value differs from some
  default value. Sends an event with the button and the elapsed ms it has been
  pressed for."
  [buttons state elapsed]
  (for [[button default] buttons]
    (let [value (get state button)]
      (when (and (some? value) (not= value default))
        {:type :button/repeat
         :source :gamepad
         :button button
         :value value
         :elapsed elapsed}))))



;;;;; NES CONTROLLER ;;;;;

; Wait, why are these the same as the SNES controller?
(def ^:const nes-vendor-id  0x12bd)
(def ^:const nes-product-id 0xd015)


; TODO: implementation
; Looks like buffer bytes 0 and 1 are the same x and y axis values
; byte 3 gives A and B as the first two bits
; byte 4 gives select and start as the fist two bits



;;;;; SNES CONTROLLER ;;;;;

(def ^:const snes-vendor-id  0x12bd)
(def ^:const snes-product-id 0xd015)


(def ^:private snes-bits
  {:X      [3 0]
   :A      [3 1]
   :B      [3 2]
   :Y      [3 3]
   :L      [3 4]
   :R      [3 5]
   :select [4 0]
   :start  [4 1]})


(def ^:private snes-defaults
  {:x-axis 0.0
   :y-axis 0.0
   :X      false
   :A      false
   :B      false
   :Y      false
   :L      false
   :R      false
   :select false
   :start  false})


(defn- snes-read-state
  "SNES controller state is read 8 bytes at a time:
    byte 0: x-axis [00 7F FF] (FF is right)
    byte 1: y-axis [00 7F FF] (FF is down)
    byte 2: always 00
    byte 3: X, A, B, Y, L, R
    byte 4: select, start
    bytes 5-7: always 00"
  [^bytes buffer len]
  (if (< len 8)
    (log/warn
      (str "Incomplete data read from SNES controller: "
           (apply str (map (partial format "%02X") (take len buffer)))))
    (assoc
      (read-buttons snes-bits buffer)
      :x-axis (byte-axis (aget buffer 0))
      :y-axis (- (byte-axis (aget buffer 1))))))


(defn- snes-state-events
  [old-state new-state elapsed]
  (remove nil?
    (concat
      (repeat-events snes-defaults old-state elapsed)
      (button-events snes-defaults old-state new-state))))


(defn snes
  "Creates a new SNES gamepad input which will send button events to the given
  channel."
  [channel]
  {:pre [(some? channel)]}
  ; TODO: sometimes nil when refreshing code...
  (if-let [device (usb/find-device snes-vendor-id snes-product-id)]
    (usb/hid-input
      :snes
      channel
      device
      snes-read-state
      snes-state-events)))
