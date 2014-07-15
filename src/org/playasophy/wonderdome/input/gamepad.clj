(ns org.playasophy.wonderdome.input.gamepad
  "A gamepad input watches for button presses from a USB game controller."
  (:require
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


(defn- button-events
  "Calculates button press and release events. Returns a seq of events."
  [buttons old-state new-state]
  (for [button buttons]
    (let [old-val (get old-state button)
          new-val (get new-state button)]
      (cond
        (and new-val (not old-val))
        {:type :gamepad/press, :button button}

        (and old-val (not new-val))
        {:type :gamepad/release, :button button}))))


(defn- repeat-events
  "Calculates repeated button 'hold' events, where the value differs from some
  default value. Sends an event with the button and the elapsed ms it has been
  pressed for."
  [defaults state elapsed]
  (for [[button default] defaults]
    (let [value (get state button)]
      (when (and (some? value) (not= value default))
        {:type :gamepad/repeat
         :button button
         :value value
         :elapsed elapsed}))))



;;;;; NES CONTROLLER ;;;;;

(def ^:const nes-vendor-id  0x12bd)
(def ^:const nes-product-id 0xd015)


; TODO: implementation



;;;;; SNES CONTROLLER ;;;;;

(def ^:const snes-vendor-id  0x12bd)
(def ^:const snes-product-id 0xd015)


(defn- snes-read-state
  "SNES controller state is read 8 bytes at a time:
    byte 0: x-axis [00 7F FF] (FF is right)
    byte 1: y-axis [00 7F FF] (FF is down)
    byte 2: always 00
    byte 3:
      01 X
      02 A
      04 B
      08 Y
      10 left shoulder
      20 right shoulder
    byte 4:
      01 select
      02 start
    bytes 5-7: always 00"
  [buffer len]
  (if (< len 8)
    (println "Incomplete data read from SNES controller:"
             (apply str (map (partial format "%02X") (take len buffer))))
    {:x-axis (byte-axis (aget buffer 0))
     :y-axis (- (byte-axis (aget buffer 1)))
     :X (bit-test (aget buffer 3) 0)
     :A (bit-test (aget buffer 3) 1)
     :B (bit-test (aget buffer 3) 2)
     :Y (bit-test (aget buffer 3) 3)
     :L (bit-test (aget buffer 3) 4)
     :R (bit-test (aget buffer 3) 5)
     :select (bit-test (aget buffer 4) 0)
     :start  (bit-test (aget buffer 4) 1)}))


(defn- snes-state-events
  [old-state new-state elapsed]
  (let [axes {:x-axis 0.0, :y-axis 0.0}
        buttons (remove (keys axes) (keys new-state))]
    (remove nil?
      (concat
        (repeat-events axes old-state elapsed)
        (button-events buttons old-state new-state)))))


(defn snes
  "Creates a new SNES gamepad input which will send button press events to the
  given channel."
  [channel]
  {:pre [(some? channel)]}
  (usb/hid-input
    channel
    ; TODO: sometimes nil when refreshing code...
    (usb/find-device snes-vendor-id snes-product-id)
    snes-read-state
    snes-state-events))
