(defproject playasophy/wonderdome "2.0.0-SNAPSHOT"
  :description "Control and rendering software for driving an LED strip art project with various visualizations."
  :url "https://github.com/playasophy/wonderdome"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  ; TODO: write some simple dev code to search for dot files in the doc folder and generate SVG graphs.
  ;:aliases {"sysgraph" ["dot" "-Tsvg" "<" "doc/system-processes.dot" ">" "target/system-processes.svg"]}
  :aliases {"scalac" ["shell" "./util/build-scala.sh"]}

  :pedantic? :warn

  :repositories
  [["playasophy" "http://playasophy.org/repositories/jars"]]

  :plugins
  [[lein-shell "0.5.0"]]

  :dependencies
  [[org.clojure/clojure "1.8.0"]
   [org.clojure/core.async "0.2.385"]
   [org.clojure/tools.logging "0.3.1"]
   [org.clojure/tools.reader "1.0.0-beta2"]

   [org.scala-lang/scala-library "2.11.7"]

   [com.codeminders/hidapi "1.1"]
   [com.heroicrobot/pixelpusher "20130916"
    :exclusions [joda-time]]
   [ddf.minim "2.2.0"]

   [org.slf4j/jul-to-slf4j "1.7.21"]
   [ch.qos.logback/logback-classic "1.1.7"]

   [compojure "1.5.1"]
   [com.stuartsierra/component "0.3.1"]
   [hiccup "1.0.5"]
   [org.slf4j/jul-to-slf4j "1.7.21"]
   [ring/ring-core "1.5.0"]
   [ring/ring-jetty-adapter "1.5.0"]]

  :prep-tasks ["javac" "scalac" "compile"]
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :scala-source-path "src/scala"
  :native-path "target/native"

  :jvm-opts ^:replace ["-Djava.library.path=target/native/linux"]

  :hiera
  {:cluster-depth 4
   :vertical? false
   :show-external? false
   :ignore-ns #{user}}

  :profiles
  {:repl
   {:source-paths ["dev"]
    :dependencies [[quil "2.2.6"]
                   [org.clojure/tools.namespace "0.2.10"]]
    :jvm-opts ["-DLOGBACK_APPENDER=repl"
               "-DWONDERDOME_LOG_LEVEL=DEBUG"]}

   :test
   {:jvm-opts ["-DLOGBACK_APPENDER=nop"
               "-DWONDERDOME_LOG_LEVEL=TRACE"] }

   :uberjar
   {:target-path "target/uberjar"
    :main playasophy.wonderdome.main
    :aot [playasophy.wonderdome.main]}})
