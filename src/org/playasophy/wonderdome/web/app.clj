(ns org.playasophy.wonderdome.web.app
  (:require
    [clojure.string :as str]
    (compojure
      [core :refer [ANY GET POST routes]]
      [route :as route])
    (ring.middleware
      [keyword-params :refer [wrap-keyword-params]]
      [params :refer [wrap-params]])
    [ring.util.response :refer [header response status]]))


;;;;; RESPONSE FUNCTIONS ;;;;;

(defn- method-not-allowed
  [& allowed]
  (-> (response nil)
      (header "Allow" (str/join ", " (map (comp str/upper-case name) allowed)))
      (status 405)))



;;;;; APPLICATION CONSTRUCTORS ;;;;;

(defn app-routes
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


(defn wrap-middleware
  "Wraps the application routes in middleware."
  [handler]
  (-> handler
      wrap-keyword-params
      wrap-params
      ; TODO: add middlewares:
      ; wrap-exception-handler
      ; wrap-request-logger
      ; wrap-x-forwarded-for
      ))
