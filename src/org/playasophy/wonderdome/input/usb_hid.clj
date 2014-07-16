(ns org.playasophy.wonderdome.input.usb-hid
  "The USB HID component reads state from a device and reports button events."
  (:require
    [clojure.core.async :as async :refer [>!!]]
    [com.stuartsierra.component :as component])
  (:import
    (com.codeminders.hidapi
      HIDDevice
      HIDManager)))



;;;;; CONFIGURATION ;;;;;

(def ^:private library-name
  "JNI library to load when finding devices."
  "hidapi-jni-64")


(def ^:private ^:const poll-period
  "Milliseconds to wait for new input from USB device."
  100)


(def ^:private ^:const buffer-size
  "Byte buffer size for reading USB input state."
  256)



;;;;; HELPER FUNCTIONS ;;;;;

(defn device-info
  "Builds a map with a device's manufacturer, product, and serial number."
  [^HIDDevice device]
  (when device
    {:manufacturer (.getManufacturerString device)
     :product (.getProductString device)
     :serial (.getSerialNumberString device)}))


(defn find-device
  [vendor-id product-id]
  (try
    (clojure.lang.RT/loadLibrary library-name)
    ; TODO: release the manager?
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
  [^HIDDevice device read-state state-events channel]
  (let [buffer (byte-array buffer-size)

        read-device!
        (fn []
          (let [len (.readTimeout device buffer poll-period)]
            (when (pos? len)
              (read-state buffer len))))

        put-events!
        (fn [events]
          (dorun (map (partial >!! channel)
                      (remove nil? events))))]
    (fn []
      (try
        (loop [t (System/currentTimeMillis)
               old-state nil]
          (when-not (Thread/interrupted)
            (let [new-state (or (read-device!) old-state)
                  now (System/currentTimeMillis)]
              (put-events! (state-events old-state new-state (- now t)))
              (recur now new-state))))
        (catch InterruptedException e
          nil)))))



;;;;; INPUT COMPONENT ;;;;;

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
    #_ ; TODO: this will prevent the component from being started again...
    (when device
      (.close device))
    (when process
      (.interrupt process)
      (.join process 1000))
    (assoc this :process nil)))


(defn hid-input
  "Creates a new USB input which will report state changes to the function."
  [channel device read-state state-events]
  {:pre [(some? channel)
         (some? device)
         (fn? read-state)
         (fn? state-events)]}
  (HIDInput. channel device read-state state-events nil))
