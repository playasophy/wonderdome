(defproject playasophy/wonderdome "2.0.0"
  :description "Control and rendering software for driving an LED strip art project with various visualizations."
  :url "https://github.com/playasophy/wonderdome"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :native-path "target/native"

  :jvm-opts ^:replace ["-Djava.library.path=target/native/linux"]

  ;:aot [playasophy.wonderdome.input.audio]

  :repositories
  [["mvxcvi" "http://mvxcvi.com/libs/repo"]]

  :dependencies
  [[ch.qos.logback/logback-classic "1.1.3"]
   [compojure "1.3.4"]
   [com.codeminders/hidapi "1.1"]
   [com.heroicrobot/pixelpusher "20130916"]
   [com.stuartsierra/component "0.2.3"]
   [ddf.minim "2.2.0"]
   [hiccup "1.0.5"]
   [org.clojure/clojure "1.6.0"]
   [org.clojure/core.async "0.1.303.0-886421-alpha"]
   [org.clojure/tools.logging "0.3.1"]
   [org.slf4j/jul-to-slf4j "1.7.12"]
   [ring/ring-core "1.3.2"]
   [ring/ring-jetty-adapter "1.3.2"]]

  :hiera
  {:cluster-depth 4
   :vertical? false
   :show-external? false
   :ignore-ns #{user}}

  ; TODO: write some simple dev code to search for dot files in the doc folder and generate SVG graphs.
  ;:aliases {"sysgraph" ["dot" "-Tsvg" "<" "doc/system-processes.dot" ">" "target/system-processes.svg"]}

  :profiles
  {:dev
   {:source-paths ["dev"]
    :dependencies [[quil "2.2.6"]
                   [org.clojure/tools.namespace "0.2.10"]
                   [ring/ring-devel "1.3.2"]]
    :jvm-opts ["-DLOGBACK_APPENDER=repl"
               "-DWONDERDOME_LOG_LEVEL=DEBUG"]}

   :test {:jvm-opts ["-DLOGBACK_APPENDER=nop"
                     "-DWONDERDOME_LOG_LEVEL=TRACE"] }

   :uberjar
   {:aot :all
    :target-path "target/uberjar"
    :main playasophy.wonderdome.main}})
