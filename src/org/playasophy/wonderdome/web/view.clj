(ns org.playasophy.wonderdome.web.view
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.string :as str]))


;;;;; TEMPLATE FUNCTIONS ;;;;;

(defn- key->id
  "Converts a keyword into an html-safe identifier."
  [k]
  (let [clean #(str/replace % \. \-)]
    (str
      (and (namespace k) (str (clean (namespace k)) \-))
      (clean (name k)))))


(defn- button
  ([name]
   (button name name))
  ([name text & {:keys [cls] :or {cls "btn btn-default"}}]
   [:button {:type "button" :class cls :name name} text]))


(defn- head
  "Generates an html <head> section."
  [title & extra]
  [:head
   [:title title]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   [:link {:href "/css/bootstrap.min.css" :rel "stylesheet" :media "screen"}]
   [:script {:src "/js/jquery.min.js"}]
   extra])


(def ^:const ^:private navbar
  [:div.navbar.navbar-inverse.navbar-fixed-top
   [:div.container
    [:div.navbar-header
     [:a.navbar-brand {:href "/"} "Wonderdome"]]
    [:div.collapse.navbar-collapse
     [:ul.nav.navbar-nav
      [:li [:a {:href "/about"} "About"]]
      [:li [:a {:href "/system"} "System Status"]]
      [:li [:a {:href "mailto:wonderdome@playasophy.org?subject=Feedback"} "Send us feedback!"]]]]]])


(defn- page
  "Generates an html page based on the standard template."
  [head-content body-content]
  [:html
   head-content
   [:body {:style "padding-top: 50px;"}
    navbar
    [:div.container
     body-content]]])



;;;;; PAGE VIEWS ;;;;;

(def about
  (page
    (head "Wonderdome - About")
    [:div.about {:style "padding: 40px 15px;"}
     [:h1 "About the Wonderdome"]
     [:p
      "The Wonderdome is Playasophy Camp's first major venture into LED-based
      eye candy. It consists of 6 strips of 240 LEDs for a total of 1440, each
      of which can be individually controlled in full 24-bit color."]

     [:h2 "Hardware"]
     [:p
      "The Wonderdome is enclosed in a weather-resistant plastic shell with
      ports mounted on the side to hook up power and networking. The system
      includes two power supplies - one that accepts standard 120VAC generator
      power and another that takes 12VDC from a deep-cycle battery. Both power
      supplies convert to the 5VDC that the rest of the system uses."]
     [:p
      "The LED display is built around the "
      [:a {:href "http://www.heroicrobotics.com/products/pixelpusher"} "HeroicRobotics PixelPusher"]
      " and "
      [:a {:href "http://www.illumn.com/other-products/pixelpusher-and-led-strips.html"} "LPD8806 RGB LED strips"]
      ". The PixelPusher presents itself as a network device and listens for
      UDP broadcasts to register with a controller. The controller sends UDP
      packets to the PixelPusher to give pixel color-setting commands."]
     [:p
      "The computer 'brain' running the system is an "
      [:a {:href "http://hardkernel.com/main/products/prdt_info.php"} "ODROID-U3"]
      ", which is a quad-core Snapdragon - the same hardware found in many modern smartphones."]

     [:h2 "Software"]
     [:p
      "The software driving the display is Clojure code running on the JVM. The overall
      system is composed of many individual components communicating via `core.async`
      channels."]]))


(defn system-stats
  [stats]
  (let [stat (fn [k] [:span {:id (key->id k)} (get stats k)])
        ->mb (fn [v] (format "%.0f MB" (/ v 1024 1024.0)))]
    (page
      (head "Wonderdome - System")
      [:div.system {:style "padding: 40px 15px;"}
       [:h1 "System Status"]
       [:ul
        [:li [:strong "Operating System"] " "
         (stat :os/name) " "
         (stat :os/version) " ("
         (stat :os/arch) ")"]
        [:li [:strong "Java Virtual Machine"] " "
         (stat :java/vm-name) " "
         (stat :java/version)]
        [:li [:strong "Physical Memory"] " "
         (let [total (or (:memory.physical/total stats) 1)
               free (or (:memory.physical/free stats) 0)
               used (- total free)]
            (format
              "%s / %s (%.0f%% in use)"
              (->mb used)
              (->mb total)
              (* 100.0 (/ used total))))]
        [:li [:strong "Swap Space"] " "
         (let [total (or (:memory.swap/total stats) 1)
               free (or (:memory.swap/free stats) 0)
               used (- total free)]
            (format "%s / %s (%.0f%% in use)"
                    (->mb used)
                    (->mb total)
                    (* 100.0 (/ used total))))]
        [:li [:strong "Threads"] " "
         (format "%d running (%d daemons)"
                 (:thread/count stats)
                 (:thread/daemons stats))]]
       [:h3 "Raw stats"]
       [:div#raw-stats {:style "display: block;"}
        [:pre (with-out-str (pprint stats))]]])))


(def admin
  (page
    (head "Wonderdome - Admin"
      [:script
       "$(document).ready(function() {
          $('button').click(function() {
            $.post('/admin', {action: $(this).attr('name')});
          });
        });"])
    [:div.admin {:style "padding: 40px 15px; text-align: center;"}
     [:h1 "Wonderdome Administration"]
     (map button ["pause" "resume" "terminate"])]))


(def control
  (page
    (head "Wonderdome - Controls"
      [:script
       "$(document).ready(function(){
          $('button').mousedown(function(){
            $.post('/events',
            {
              button:$(this).attr('name'),
              type:'button.press'
            });
          });
          $('button').mouseup(function(){
            $.post('/events',
            {
              button:$(this).attr('name'),
              type:'button.release'
            });
          });
        });"])
    [:div.admin {:style "padding: 40px 15px; text-align: center;"}
     [:h1 "Wonderdome Controls"]
     ; TODO: directions need to be handled differently
     [:p (map button ["up" "down" "left" "right"])]
     [:p (map button ["select" "start"])]
     [:p (map button ["A" "B" "X" "Y"])]
     [:p (map button ["L" "R"])]]))
