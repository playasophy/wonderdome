(ns org.playasophy.wonderdome.web.app
  (:require
    [clojure.string :as str]
    (compojure
      [core :refer [ANY GET POST routes]]
      [route :refer [not-found]])
    [com.stuartsierra.component :as component]
    [org.playasophy.wonderdome.web.middleware :refer :all]
    [ring.adapter.jetty :as jetty]
    (ring.middleware
      [keyword-params :refer [wrap-keyword-params]]
      [params :refer [wrap-params]])
    [ring.util.response :as r])
  (:import
    org.eclipse.jetty.server.Server))


;;;;; RESPONSE FUNCTIONS ;;;;;

(defn- method-not-allowed
  [& allowed]
  (-> (r/response nil)
      (r/header "Allow" (str/join ", " (map (comp str/upper-case name) allowed)))
      (r/status 405)))



;;;;; APPLICATION CONSTRUCTORS ;;;;;

(defn app-routes
  "Constructs a new Ring handler implementing the website application."
  [config]
  (routes
    (GET "/" []
      (r/response "Hello World"))
    (ANY "/" []
      (method-not-allowed :get))

    (GET "/about" []
      (r/response "TODO: render about page"))
    (ANY "/about" []
      (method-not-allowed :get))

    ; TODO: input endpoint

    (GET "/admin" []
      (r/response "TODO: render admin page"))
    (ANY "/admin" []
      (method-not-allowed :get))

    (not-found {:message "Not Found"})))


(defn wrap-middleware
  "Wraps the application routes in middleware."
  [handler]
  (-> handler
      wrap-keyword-params
      wrap-params
      wrap-exception-handler
      wrap-request-logger
      wrap-x-forwarded-for))



;;;;; WEB SERVER ;;;;;

(defrecord WebServer
  [^Server server]

  component/Lifecycle

  (start
    [this]
    (if server
      (do
        (when-not (.isStarted server)
          (.start server))
        this)
      (let [handler (wrap-middleware (app-routes nil))
            options {:port 8080
                     :host "0.0.0.0"
                     :join? false
                     :min-threads 3
                     :max-threads 10
                     :max-queued 25}]
        (assoc this :server (jetty/run-jetty handler options)))))


  (stop
    [this]
    (when (and server (not (.isStopped server)))
      (.stop server))
    this))


(defn server
  "Constructs a new web server component with the given options."
  []
  (WebServer. nil))
