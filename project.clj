(defproject playasophy/wonderdome "2.0.0-SNAPSHOT"
  :description "Control and rendering software for driving an LED strip art project with various visualizations."
  :url "https://github.com/playasophy/wonderdome"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :native-path "target/native"
  :jvm-opts ^:replace ["-Djava.library.path=target/native/linux"]

  ; TODO: write some simple dev code to search for dot files in the doc folder and generate SVG graphs.
  ;:aliases {"sysgraph" ["dot" "-Tsvg" "<" "doc/system-processes.dot" ">" "target/system-processes.svg"]}

  :repositories
  [["mvxcvi" "http://mvxcvi.com/libs/repo"]]

  :dependencies
  [[ch.qos.logback/logback-classic "1.1.2"]
   [compojure "1.1.8"]
   [com.codeminders/hidapi "1.1"]
   [com.heroicrobot/pixelpusher "20130916"]
   [com.stuartsierra/component "0.2.1"]
   [hiccup "1.0.5"]
   [org.clojure/clojure "1.6.0"]
   [org.clojure/core.async "0.1.303.0-886421-alpha"]
   [org.clojure/tools.logging "0.3.0"]
   [org.slf4j/jul-to-slf4j "1.7.6"]
   [potemkin "0.3.4"]
   [ring/ring-core "1.3.0"]
   [ring/ring-jetty-adapter "1.3.0"]]

  :hiera
  {:cluster-depth 4
   :vertical? false
   :show-external? false
   :ignore-ns #{user}}

  :profiles
  {:dev
   {:source-paths ["dev"]
    :dependencies
    [[quil "2.1.0"]
     [org.clojure/tools.namespace "0.2.5"]
     [ring/ring-devel "1.3.0"]]}

   :uberjar
   {:aot :all
    :main org.playasophy.wonderdome.main}})
