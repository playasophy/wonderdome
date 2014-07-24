(ns org.playasophy.wonderdome.web.app
  (:require
    [clojure.string :as str]
    (compojure
      [core :refer [ANY GET POST routes]]
      [route :as route])
    [ring.util.response :refer [header response status]]))


;;;;; RESPONSE FUNCTIONS ;;;;;

(defn- method-not-allowed
  [& allowed]
  (-> (response nil)
      (header "Allow" (str/join ", " (map (comp str/upper-case name) allowed)))
      (status 405)))



;;;;; APPLICATION ROUTES ;;;;;

(defn ->site
  "Constructs a new Ring handler implementing the website application."
  [config]
  (routes
    (GET "/" []
      (response "Hello World"))
    (ANY "/" []
      (method-not-allowed :get))

    (GET "/about" []
      (response "TODO: render about page"))
    (ANY "/about" []
      (method-not-allowed :get))

    ; TODO: input endpoint

    (GET "/admin" []
      (response "TODO: render admin page"))
    (ANY "/admin" []
      (method-not-allowed :get))

    (route/not-found {:message "Not Found"})))
