(ns playasophy.wonderdome.input.gamepad
  "A gamepad input watches for button presses from a USB game controller."
  (:require
    [clojure.tools.logging :as log]
    [playasophy.wonderdome.input.usb-hid :as usb]))


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


(defn- button-state-events
  "Calculates button press and release events. Returns a sequence of button
  press and release events."
  [buttons old-state new-state]
  (for [button buttons]
    (let [old-val (get old-state button)
          new-val (get new-state button)]
      (when (not= old-val new-val)
        {:type (if new-val :button/press :button/release)
         :source :gamepad
         :input button}))))


(defn- button-hold-events
  "Calculates repeated button hold events. Returns a sequence of events with
  the button and the elapsed ms it has been pressed for."
  [buttons state elapsed]
  (for [button buttons]
    (if (get state button)
      {:type :button/hold
       :source :gamepad
       :input button
       :elapsed elapsed})))


(defn- axis-direction-events
  "Calculates repeated axis direction events. Returns a sequence of events with
  the axis, the current value in [-1.0, 1.0], and the elapsed ms it has had
  that value."
  [axes state elapsed]
  (for [[axis default] axes]
    (let [value (get state axis)]
      (when (not= default value)
        {:type :axis/direction
         :source :gamepad
         :input axis
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



;;;;; ORIGINAL SNES CONTROLLER ;;;;;

(def ^:const og-snes-vendor-id  0x12bd)
(def ^:const og-snes-product-id 0xd015)

(def ^:private og-snes-axes
  {:x-axis 0.0
   :y-axis 0.0})

(def ^:private og-snes-buttons
  #{:X :A :B :Y :L :R :select :start :left :right :up :down})

(def ^:private og-snes-bits
  {:X      [3 0]
   :A      [3 1]
   :B      [3 2]
   :Y      [3 3]
   :L      [3 4]
   :R      [3 5]
   :select [4 0]
   :start  [4 1]})


(defn- og-snes-read-state
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
    (let [x-axis (byte-axis (aget buffer 0))
          y-axis (- (byte-axis (aget buffer 1)))]
      (assoc
        (read-buttons og-snes-bits buffer)
        :x-axis x-axis
        :y-axis y-axis
        :left  (neg? x-axis)
        :right (pos? x-axis)
        :down  (neg? y-axis)
        :up    (pos? y-axis)))))


(defn- og-snes-state-events
  [old-state new-state elapsed]
  (remove nil?
    (concat
      (axis-direction-events og-snes-axes old-state elapsed)
      (button-hold-events og-snes-buttons old-state elapsed)
      (button-state-events og-snes-buttons old-state new-state))))


(defn og-snes
  "Creates a new SNES gamepad input which will send button events to the given
  channel."
  [channel]
  {:pre [(some? channel)]}
  ; TODO: sometimes nil when refreshing code...
  (if-let [device (usb/find-device og-snes-vendor-id og-snes-product-id)]
    (usb/hid-input
      :snes
      channel
      device
      og-snes-read-state
      og-snes-state-events)))



;;;;; NEW SNES CONTROLLER ;;;;;

(def ^:const snes-vendor-id  2064)
(def ^:const snes-product-id 58625)

(def ^:private snes-axes
  {:x-axis 0.0
   :y-axis 0.0})

(def ^:private snes-buttons
  #{:X :A :B :Y :L :R :select :start :left :right :up :down})

(def ^:private snes-bits
  {:X      [5 4]
   :A      [5 5]
   :B      [5 6]
   :Y      [5 7]
   :L      [6 0]
   :R      [6 1]
   :select [6 4]
   :start  [6 5]})


(defn- snes-read-state
  "SNES controller state is read 8 bytes at a time:
    byte 0: always 01
    byte 1: always 80
    byte 2: always 80
    byte 3: x-axis [00 7F FF] (FF is right)
    byte 4: y-axis [00 7F FF] (FF is down)
    byte 5: 0F + X (10), A (20), B (40), Y (80)
    byte 6: L (1), R (2), select (10), start (20)
    byte 7: always 00"
  [^bytes buffer len]
  (if (< len 8)
    (log/warn
      (str "Incomplete data read from SNES controller: "
           (apply str (map (partial format "%02X") (take len buffer)))))
    (let [x-axis (byte-axis (aget buffer 3))
          y-axis (- (byte-axis (aget buffer 4)))]
      (assoc
        (read-buttons snes-bits buffer)
        :x-axis x-axis
        :y-axis y-axis
        :left  (neg? x-axis)
        :right (pos? x-axis)
        :down  (neg? y-axis)
        :up    (pos? y-axis)))))


(defn- snes-state-events
  [old-state new-state elapsed]
  (remove nil?
    (concat
      (axis-direction-events snes-axes old-state elapsed)
      (button-hold-events snes-buttons old-state elapsed)
      (button-state-events snes-buttons old-state new-state))))


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
