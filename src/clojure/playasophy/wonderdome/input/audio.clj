(ns playasophy.wonderdome.input.audio
  "Audio inputs listen to an input stream of audio samples and sends frequency
  band power and beat-detection events."
  (:require
    [clojure.core.async :as async :refer [>!!]]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component])
  (:import
    (ddf.minim
      Minim)
    (ddf.minim.analysis
      BeatDetect
      FFT)
    (java.io
      FileInputStream
      InputStream)
    playasophy.wonderdome.input.FileSystemHandler))


(def ^:private buffer-size
  "Audio buffer size, in bytes."
  1024)



;;;;; HELPER FUNCTIONS ;;;;;

(defn- get-line-in
  "Acquires an audio line input and returns the constructed Minim and input
  objects."
  []
  (let [fsh (FileSystemHandler. (System/getProperty "user.dir"))
        minim (Minim. fsh)
        input (.getLineIn minim Minim/MONO buffer-size 44100.0 16)]
    (when-not input
      (.stop minim)
      (throw (IllegalStateException. "Unable to get an audio line input")))
    [minim input]))


(defn- audio-loop
  "Constructs a new runnable looping function which puts events on the given
  output channel. The loop can be terminated by interrupting the thread."
  ^Runnable
  [^long period ^ddf.minim.AudioInput input channel]
  (let [^FFT fft (doto (FFT. (.bufferSize input) (.sampleRate input))
                   (.logAverages 50 3)
                   (.window FFT/GAUSS))
        ^BeatDetect beats (BeatDetect.)]
    (fn []
      (try
        (loop []
          (when-not (Thread/interrupted)
            (Thread/sleep period)
            (let [buffer (.mix input)]
              (.detect beats buffer)
              (when (.isOnset beats)
                (>!! channel {:type :audio/beat, :power (.level buffer)}))
              (.forward fft buffer)
              (let [spectrum (vec (map #(.getAvg fft %) (range (.avgSize fft))))]
                (>!! channel {:type :audio/freq, :spectrum spectrum})))
            (recur)))
        (catch InterruptedException e
          nil)))))



;;;;; AUDIO INPUT COMPONENT ;;;;;

(defrecord AudioInput
  [channel
   period
   ^Minim minim
   ^ddf.minim.AudioInput input
   ^Thread process]

  component/Lifecycle

  (start
    [this]
    (if process
      (do
        (log/info "AudioInput already started")
        this)
      (try
        (log/info (str "Starting AudioInput..."))
        (if-let [[minim input] (get-line-in)]
          (assoc this
            :minim minim
            :input input
            :process
            (doto (Thread. ^Runnable (audio-loop period input channel) "AudioInput")
              (.setDaemon true)
              (.start)))
          this)
        (catch Exception e
          (log/error e "Failed to start AudioInput")
          this))))


  (stop
    [this]
    (log/info "Stopping AudioInput...")
    (when process
      (.interrupt process)
      (.join process 1000))
    (when input
      (.close input))
    (when minim
      (.stop minim))
    (assoc this
      :process nil
      :input nil
      :minim nil)))


(defn audio-input
  "Creates a new audio input that will analyze the sound spectrum every period
  milliseconds. Beat detection and frequency spectrum events will be put on the
  given channel."
  [channel period]
  {:pre [(some? channel) (pos? period)]}
  (->AudioInput channel period nil nil nil))
