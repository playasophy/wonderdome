(ns org.playasophy.wonderdome.input.audio
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
    org.playasophy.wonderdome.input.FileSystemHandler))


(def ^:private buffer-size
  "Audio buffer size, in bytes."
  1024)



;;;;; AUDIO PROCESSING LOOP ;;;;;

(defn- audio-loop
  "Constructs a new runnable looping function which puts events on the given
  output channel. The loop can be terminated by interrupting the thread."
  ^Runnable
  [^long period ^ddf.minim.AudioInput input channel]
  (let [^FFT fft (FFT. (.bufferSize input) (.sampleRate input))
        ^BeatDetect beats (BeatDetect.)]
    (.logAverages fft 50 3)
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
      (do
        (log/info (str "Starting AudioInput..."))
        (let [minim (Minim. (FileSystemHandler. (System/getProperty "user.dir")))
              input (.getLineIn minim Minim/MONO buffer-size)]
        (assoc this
          :minim minim
          :input input
          :process
          (doto (Thread. (audio-loop period input channel) "AudioInput")
            (.setDaemon true)
            (.start)))))))


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
  (AudioInput. channel period nil nil nil))
