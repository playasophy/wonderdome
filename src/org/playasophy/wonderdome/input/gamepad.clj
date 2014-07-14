(ns org.playasophy.wonderdome.input.gamepad
  "A gamepad input watches for button presses from a USB game controller."
  (:require
    [clojure.core.async :as async :refer [>!!]]
    [com.stuartsierra.component :as component])
  (:import
    (com.codeminders.hidapi
      HIDDevice
      HIDManager)))


(def nes-vendor-id   4797)
(def nes-product-id 53269)

(def snes-vendor-id  0x12bd)
(def snes-product-id 0xd015)


(defn get-device
  ^HIDDevice
  [vendor-id product-id]
  (try
    (clojure.lang.RT/loadLibrary "hidapi-jni")
    (-> (HIDManager/getInstance)
        (.openById vendor-id product-id nil))
    #_ (catch UnsatisfiedLinkError e
      (println "Failed to load USB library:" e)
      nil)
    (catch RuntimeException e
      (println "Error loading gamepad input device:" e)
      nil)))


(defn read-device
  "Returns a state map showing the current buttons pressed."
  [^HIDDevice device ^bytes buffer]
  (let [len (.readTimeout device buffer 1000)]
    (println (apply str "Read " len " bytes of output from gamepad: "
                   (map (partial format "%02X") buffer)))
    ; TODO: build state map
    {}))


(defn- input-loop
  "Constructs a new runnable looping function which reads the gamepad state
  and sends any detected button presses. The loop can be terminated by
  interrupting the thread."
  ^Runnable
  [device buffer channel]
  (fn []
    (try
      (loop []
        ;(Thread/sleep period)
        (let [state (read-device device buffer)]
          ; TODO: calculate individual button events, rather than sending entire state
          (>!! channel {:type :gamepad, :buttons state}))
        (recur))
      (catch InterruptedException e
        nil))))


(defrecord GamepadInput
  [channel ^HIDDevice device ^Thread process]

  component/Lifecycle

  (start
    [this]
    (if process
      this
      (assoc this :process
        (doto (Thread. (input-loop device (byte-array 512) channel) "GamepadInput")
          (.setDaemon true)
          (.start)))))


  (stop
    [this]
    (when process
      (.interrupt process)
      (.join process 1000))
    (assoc this :process nil))


  Object

  (toString
    [this]
    (if device
      (str (.getManufacturerString device) "/"
           (.getProductString device))
      "(no device)")))


(defn gamepad
  "Creates a new gamepad input which will send button press events to the given
  channel."
  [channel vendor-id product-id]
  {:pre [(some? channel)]}
  (GamepadInput. channel (get-device vendor-id product-id) nil))
