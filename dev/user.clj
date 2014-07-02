(ns user
  (:require
    [clj-time.core :as time]
    [clojure.java.io :as io]
    [clojure.pprint :refer [pprint]]
    [clojure.repl :refer :all]
    [clojure.string :as str]
    [clojure.tools.namespace.repl :refer [refresh]]
    [environ.core :refer [env]]
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.core :as wonder]
    [org.playasophy.wonderdome.display.sketch :as sketch]))


(def config
  {:layout nil
   :display (sketch/processing-display 800 450)
   :modes []})


(def system nil)


(defn start!
  "Initialize the wonderdome for local development."
  []
  (alter-var-root #'system
    (constantly (wonder/initialize config))))


(defn stop!
  "Stops the wonderdome system and closes the display window."
  []
  ; TODO: implement
  nil)


(defn reload!
  []
  (stop!)
  (refresh :after 'user/start!))
