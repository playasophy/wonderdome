(defproject playasophy/wonderdome "1.0.0-SNAPSHOT"
  :description "Control and rendering software for driving an LED strip art project with various visualizations."
  :url "https://github.com/playasophy/wonderdome"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :native-path "target/native"
  :jvm-opts ^:replace ["-Djava.library.path=target/native/linux"]

  :dependencies
  [[com.codeminders/hidapi "1.1"]
   [com.stuartsierra/component "0.2.1"]
   [org.clojure/clojure "1.6.0"]
   [org.clojure/core.async "0.1.303.0-886421-alpha"]]

  :hiera
  {:cluster-depth 4
   :vertical? false
   :ignore-ns #{com.stuartsierra quil}}

  :profiles
  {:dev
   {:source-paths ["dev"]
    :dependencies
    [[quil "2.1.0"]
     [org.clojure/tools.namespace "0.2.4"]]}})
