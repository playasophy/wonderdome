(ns org.playasophy.wonderdome.web.app
  (:require
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    (compojure
      [core :refer [ANY GET POST routes]]
      [route :refer [not-found]])
    [com.stuartsierra.component :as component]
    [hiccup.core :as hiccup]
    (org.playasophy.wonderdome.util
      [stats :as stats])
    (org.playasophy.wonderdome.web
      [middleware :refer :all]
      [view :as view])
    [ring.adapter.jetty :as jetty]
    (ring.middleware
      [content-type :refer [wrap-content-type]]
      [keyword-params :refer [wrap-keyword-params]]
      [not-modified :refer [wrap-not-modified]]
      [params :refer [wrap-params]]
      [resource :refer [wrap-resource]])
    [ring.util.response :as r])
  (:import
    org.eclipse.jetty.server.Server))


;;;;; RESPONSE FUNCTIONS ;;;;;

(defn- method-not-allowed
  [& allowed]
  (-> (r/response nil)
      (r/header "Allow" (str/join ", " (map (comp str/upper-case name) allowed)))
      (r/status 405)))


(defn- render
  "Renders a Hiccup template data structure to HTML and returns a response with
  the correct content type."
  [template]
  (-> template
      hiccup/html
      r/response
      (r/header "Content-Type" "text/html")))



;;;;; APPLICATION CONSTRUCTORS ;;;;;

(defn app-routes
  "Constructs a new Ring handler implementing the website application."
  [config]
  (routes
    (GET "/" []
      (render view/control))
    (ANY "/" []
      (method-not-allowed :get))

    (GET "/about" []
      (render view/about))
    (ANY "/about" []
      (method-not-allowed :get))

    (GET "/system" []
      (render (view/system-stats (stats/info))))
    (ANY "/system" []
      (method-not-allowed :get))

    ; TODO: input endpoint

    (GET "/admin" []
      (render view/admin))
    (ANY "/admin" []
      (method-not-allowed :get))

    (not-found {:message "Not Found"})))


(defn- wrap-middleware
  "Wraps the application routes in middleware."
  [handler]
  (-> handler
      wrap-keyword-params
      wrap-params
      (wrap-resource "/public")
      wrap-content-type
      wrap-not-modified
      wrap-exception-handler
      wrap-request-logger
      wrap-x-forwarded-for))


(defn- app-handler
  "Constructs a fully-wrapped application handler to serve with Jetty."
  [options]
  ; TODO: inject dependencies
  (let [handler (wrap-middleware (app-routes nil))]
    (if-let [wrapper (:ring/wrapper options)]
      (wrapper handler)
      handler)))



;;;;; WEB SERVER ;;;;;

(defrecord WebServer
  [options ^Server server]

  component/Lifecycle

  (start
    [this]
    (if server
      (do
        (if-not (.isStarted server)
          (do
            (log/info "Restarting WebServer...")
            (.start server))
          (log/info "WebServer is already started"))
        this)
      (let [handler (app-handler options)
            options (assoc options :host "localhost" :join? false)]
        (log/info (str "Starting WebServer on port " (:port options) "..."))
        (assoc this :server (jetty/run-jetty handler options)))))


  (stop
    [this]
    (log/info "Stopping WebServer...")
    (when (and server (not (.isStopped server)))
      (.stop server))
    this))


(defn server
  "Constructs a new web server component with the given options."
  [options]
  (WebServer. options nil))
