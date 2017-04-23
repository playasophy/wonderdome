(ns playasophy.wonderdome.config
  "Functions for loading system configuration."
  (:require
    [clojure.core.async :as async]
    [clojure.java.io :as io]
    [clojure.tools.logging :as log]
    [playasophy.wonderdome.geometry.layout :as layout]
    [playasophy.wonderdome.handler :as handler]
    (playasophy.wonderdome.input
      [audio :as audio]
      [gamepad :as gamepad]
      [timer :as timer])
    [playasophy.wonderdome.state :as state]
    [playasophy.wonderdome.util.color :as color]))


(def empty-config
  "The uninitialized config map."
  {:input-channels []
   :modes {}})


(def next-config
  "This var holds the configuration under construction."
  (atom nil))


(defn clear!
  "Removes the existing configuration."
  []
  (reset! next-config empty-config))


(defn defconfig
  "Configure the system by adding an entry to the config map."
  [k v]
  (swap! next-config assoc k v))


(defn definput
  "Associates a new input source component into a system map. The input
  function should take an output channel as the first argument. If the function
  does not return an input, the channel will be closed."
  [k input-fn channel & args]
  {:pre [(some? channel)]}
  (if-let [input (try
                   (apply input-fn channel args)
                   (catch Exception e
                     (log/error e "Failed to initialize input!")
                     nil))]
    (swap! next-config
      #(-> %
           (update-in [:input-channels] conj channel)
           (assoc-in [:inputs k] input)))
    (async/close! channel)))


(defmacro defmode
  "Initializes a new mode, assuming it's in the standard namespace location and
  the constructor is named `init`."
  [mode-name & opts]
  (let [mode-kw (keyword (name mode-name))
        mode-ns (symbol (str "playasophy.wonderdome.mode." mode-name))
        mode-var (symbol (str mode-ns) "init")]
    `(try
       (require '~mode-ns)
       (let [init-fn# (ns-resolve 'playasophy.wonderdome.config '~mode-var)]
         (swap! next-config
           assoc-in [:modes ~mode-kw]
           (init-fn# ~@opts)))
       (catch Exception ex#
         (log/error ex# ~(str "Failed to initialize mode " mode-name))))))


(defn read-file
  "Loads a system configuration file and returns the config."
  [path]
  (let [file (io/file path)]
    (if (.isDirectory file)
      (do
        (log/info "Reading config files in" file)
        (doseq [f (file-seq file)]
          (read-file (str f))))
      (try
        (log/info "Reading config file" path)
        (binding [*ns* (find-ns 'playasophy.wonderdome.config)]
          (load-file path))
        (catch Exception ex
          (log/error ex "Failed to load config file" path)))))
  @next-config)
