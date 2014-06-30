(ns org.playasophy.wonderdome.core)

;; Modes have some state, probably stored in an atom.
;; Displays are protocol objects which can be read(?) and written to, but still side effecting?
;; Loop reads in events, calls mode update.

;; There also needs to be some concept of pixel layout.
;; Does it make sense for a mode to be written for multiple layouts? Probably not.
;; All layouts may address pixels linearly, but individual layouts should have
;; more appropriate addressing. For example, square grids are best addressed
;; using (x, y) coordinates, while a geodesic grid... uh, TBD. (strut, index)?

;; A display can map a pixel index to various properties:
;; SquareGrid: x, y
;; Geodesic3V: strut, index, azimuth, elevation


(def wonderdome-state
  "Complete representation of the current wonderdome state."
  (atom {:state :running
         :last-autocycle 1234567890
         :playlist [:colorcycle
                    :sweep
                    :twinkle
                    :marqee]
         :modes {:colorcycle {:speed 0.1
                              :density 2.6
                              :offset 0.3208}
                 :sweep {:offset "..."}}
         :event-handlers []}))



