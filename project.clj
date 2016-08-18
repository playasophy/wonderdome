(defproject playasophy/wonderdome "2.0.0-SNAPSHOT"
  :description "Control and rendering software for driving an LED strip art project with various visualizations."
  :url "https://github.com/playasophy/wonderdome"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :native-path "target/native"

  ;:prep-tasks ["javac" "compile"]
  :jvm-opts ^:replace ["-Djava.library.path=target/native/linux"]

  ;:aot [playasophy.wonderdome.input.audio]
  :pedantic? :warn

  :repositories
  [["mvxcvi" "http://mvxcvi.com/libs/repo"]]

  :dependencies
  [[org.clojure/clojure "1.8.0"]
   [org.clojure/core.async "0.2.385"]
   [org.clojure/tools.logging "0.3.1"]
   [org.clojure/tools.reader "1.0.0-beta2"]

   [com.codeminders/hidapi "1.1"]
   [com.heroicrobot/pixelpusher "20130916"
    :exclusions [joda-time]]
   [ddf.minim "2.2.0"]

   [org.slf4j/jul-to-slf4j "1.7.21"]
   [ch.qos.logback/logback-classic "1.1.7"]

   [compojure "1.5.1"]
   [com.stuartsierra/component "0.3.1"]
   [hiccup "1.0.5"]
   [ring/ring-core "1.5.0"]
   [ring/ring-jetty-adapter "1.5.0"]]

  :hiera
  {:cluster-depth 4
   :vertical? false
   :show-external? false
   :ignore-ns #{user}}

  ; TODO: write some simple dev code to search for dot files in the doc folder and generate SVG graphs.
  ;:aliases {"sysgraph" ["dot" "-Tsvg" "<" "doc/system-processes.dot" ">" "target/system-processes.svg"]}

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
   {:aot :all
    :target-path "target/uberjar"
    :main playasophy.wonderdome.main}})
