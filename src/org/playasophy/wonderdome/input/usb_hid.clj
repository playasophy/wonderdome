(ns org.playasophy.wonderdome.input.usb-hid
  "The USB HID component reads state from a device and reports button events."
  (:require
    [clojure.core.async :as async :refer [>!!]]
    [com.stuartsierra.component :as component])
  (:import
    (com.codeminders.hidapi
      HIDDevice
      HIDManager)))


(def ^:private library-name
  "JNI library to load when finding devices."
  "hidapi-jni-64")


(def ^:private ^:const buffer-size
  "Byte buffer size for reading USB input state."
  256)


(defn find-device
  ^HIDDevice
  [vendor-id product-id]
  (try
    (clojure.lang.RT/loadLibrary library-name)
    (-> (HIDManager/getInstance)
        (.openById vendor-id product-id nil))
    (catch UnsatisfiedLinkError e
      (println "Failed to load USB library:" e)
      nil)
    (catch RuntimeException e
      (println "Error loading gamepad input device:" e)
      nil)))


(defn- read-loop
  "Constructs a new runnable looping function which reads the device state and
  calls the read-state function to translate the buffer into a new state map.
  The state-events function then takes the old state, new state, and time delta
  to return a sequence of events to emit. The loop can be terminated by
  interrupting the thread."
  ^Runnable
  [device read-state state-events channel]
  (let [buffer (byte-array buffer-size)]
    (fn []
      (try
        (loop [start (System/currentTimeMillis)
               old-state nil]
          (when-not (Thread/interrupted)
            (let [len (.readTimeout device buffer 100)
                  new-state (if (pos? len)
                              (read-state buffer len)
                              old-state)
                  now (System/currentTimeMillis)
                  elapsed (- now start)
                  events (state-events old-state new-state elapsed)]
              (dorun (map (partial >!! channel) (remove nil? events)))
              (recur now new-state))))
        (catch InterruptedException e
          nil)))))


(defrecord HIDInput
  [channel
   ^HIDDevice device
   read-state
   state-events
   ^Thread process]

  component/Lifecycle

  (start
    [this]
    (if process
      this
      (assoc this :process
        (doto (Thread. (read-loop device read-state state-events channel) "HIDInput")
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


(defn hid-input
  "Creates a new USB input which will report state changes to the function."
  [channel device read-state state-events]
  {:pre [(some? channel)
         (some? device)
         (fn? read-state)
         (fn? state-events)]}
  (HIDInput. channel device read-state state-events nil))
