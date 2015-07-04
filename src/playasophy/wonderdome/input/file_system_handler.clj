(ns playasophy.wonderdome.input.file-system-handler
  (:gen-class
    :name playasophy.wonderdome.input.FileSystemHandler
    :state root
    :init init
    :constructors
    {[String] []}
    :methods
    [[sketchPath [String] String]
     [createInput [String] java.io.InputStream]]))


(defn -init
  [root]
  [[] root])


(defn -sketchPath
  ^String
  [this file-name]
  (str (.root this) \/ file-name))


(defn -createInput
  [this file-name]
  (java.io.FileInputStream. (-sketchPath this file-name)))
