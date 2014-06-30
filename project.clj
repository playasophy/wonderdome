(defproject playasophy/wonderdome "1.0.0-SNAPSHOT"
  :description "Control and rendering software for driving an LED strip art project with various visualizations."
  :url "https://github.com/playasophy/wonderdome"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :target-path "build/clojure"

  :dependencies
  [[clj-time "0.7.0"]
   [environ "0.5.0"]
   [quil "2.1.0"]
   [org.clojure/clojure "1.6.0"]]

  :profiles
  {:dev
   {:dependencies
    [[com.stuartsierra/component "0.2.1"]
     [org.clojure/tools.namespace "0.2.4"]]
    :repl-options {:init-ns org.playasophy.wonderdome.simulation}}})
