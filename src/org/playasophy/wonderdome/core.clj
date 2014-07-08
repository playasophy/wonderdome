(ns org.playasophy.wonderdome.core
  (:require
    [com.stuartsierra.component :as component]))


#_
(def wonderdome-state
  "Example representation of the current wonderdome state."
  {:state :running
   :last-autocycle #inst "2014-07-07T11:03:42Z"
   :playlist [:colorcycle  ; queue is probably better
              :sweep
              :twinkle
              :marqee]
   :modes {:colorcycle {:speed 0.1
                        :density 2.6
                        :offset 0.3208}
           :sweep {:offset "..."}}
   :event-handlers []})


(defn initialize
  [{:keys [layout display modes]
    ; TODO: defaults?
    :as config}]
  (component/system-map
    :layout layout
    :display (component/using display [:layout])
    #_
    (component/using
      (example-component config-options)
      {:database  :db
       :scheduler :scheduler})))
