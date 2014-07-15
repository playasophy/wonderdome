(ns org.playasophy.wonderdome.input.gamepad
  "A gamepad input watches for button presses from a USB game controller."
  (:require
    [org.playasophy.wonderdome.input.usb-hid :as usb]))


;;;;; HELPER FUNCTIONS ;;;;;

(defn- byte-axis
  "Converts a byte value from 00 to FF into a floating-point axis value."
  [value]
  (->
    (cond
      (zero? value) 0
      (neg? value) (+ 0x101 value)
      :else (inc value))
    (/ 256.0)))


(defn- button-events
  "Determines button press and release events from old and new state maps.
  Returns a seq of button events."
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
  "Builds a function which will repeatedly fire events every period ms if their
  value differs from the default given."
  [defaults period]
  ; TODO: implement repeat events function
  ; probably store 'last repeat time' in an atom
  (constantly nil))



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
     :y-axis (- 1.0 (byte-axis (aget buffer 1)))
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
  (when (not= old-state new-state)
    (prn elapsed "ms:" new-state))
  (let [buttons (remove #{:x-axis :y-axis} (keys new-state))
        events (button-events buttons old-state new-state)]
    (dorun (map prn events))
    events))


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
