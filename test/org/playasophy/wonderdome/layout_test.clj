(ns org.playasophy.wonderdome.layout-test
  (:require
    [clojure.test :refer :all]
    [org.playasophy.wonderdome.geometry.layout :as layout]))


; FIXME: broken test
#_
(deftest normalized-coordinates
  (let [coord {:radius 0.0, :polar 0.0, :azimuth 0.0}]
    (is (= coord (layout/normalize-coord coord))))
  (let [coord {:radius 1.0, :polar (* 0.5 PI), :azimuth (* -0.5 PI)}]
    (is (= coord (layout/normalize-coord coord))))
  (is (= {:radius 2.0, :polar (* 0.5 PI), :azimuth (* -0.5 PI)}
         (layout/normalize-coord
           {:radius 2.0, :polar (* -3.5 PI), :azimuth (* 3.5 PI)})))
  (is (= {:radius 5.0, :polar (* 0.25 PI), :azimuth PI}
         (layout/normalize-coord
           {:radius 5.0, :polar (* -0.25 PI), :azimuth 0.0}))))
