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
    [org.playasophy.wonderdome.core :as wonder]))


(def config
  {:layout nil
   :modes []})


(def system nil)


(defn start!
  "Initialize the wonderdome for local development."
  []
  (alter-var-root #'system
    (constantly (wonder/initialize config))))


(defn stop!
  "Performs side effects to shut down the system and release its resources.
  Returns an updated instance of the system."
  []
  nil)


(defn reset
  []
  (stop!)
  (refresh :after 'user/start!))
