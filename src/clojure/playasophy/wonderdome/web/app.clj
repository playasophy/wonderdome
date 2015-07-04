(ns playasophy.wonderdome.web.app
  (:require
    [clojure.core.async :as async :refer [>!!]]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    (compojure
      [core :refer [ANY GET POST routes]]
      [route :refer [not-found]])
    [com.stuartsierra.component :as component]
    [hiccup.core :as hiccup]
    (playasophy.wonderdome.util
      [stats :as stats])
    (playasophy.wonderdome.web
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

(defn- bad-request
  [body]
  (-> (r/response body)
      (r/status 400)
      (r/content-type "text/plain")))


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
      (r/content-type "text/html")))


(defn- ->event
  "Constructs an event from request parameters. Throws IllegalArgumentException
  on invalid events."
  [params]
  (let [button (some-> params :button keyword)
        etype (condp = (:type params)
                "button.press"   :button/press
                "button.release" :button/release
                (throw (IllegalArgumentException.
                        (str (pr-str (:type params)) " is not a valid event type"))))]
    (case etype
      :button/press
      {:type etype, :source :web, :button (keyword (:button params))}
      :button/release
      {:type etype, :source :web, :button (keyword (:button params))})))



;;;;; APPLICATION CONSTRUCTORS ;;;;;

(defn app-routes
  "Constructs a new Ring handler implementing the website application."
  [event-channel state-agent]
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

    (POST "/events" [& params]
      (try
        (when-let [event (->event params)]
          (>!! event-channel event)
          (r/response (pr-str event)))
        (catch IllegalArgumentException e
          (bad-request (.getMessage e)))))

    (GET "/admin" []
      (render view/admin))
    (ANY "/admin" []
      (method-not-allowed :get))

    (not-found "Not Found")))


(defn- wrap-middleware
  "Wraps the application routes in middleware."
  [handler]
  (-> handler
      wrap-keyword-params
      wrap-params
      (wrap-resource "/public")
      wrap-content-type
      (wrap-cache-control #{"text/css" "text/javascript"} :max-age 300)
      wrap-not-modified
      wrap-exception-handler
      wrap-request-logger
      wrap-x-forwarded-for))


(defn- app-handler
  "Constructs a fully-wrapped application handler to serve with Jetty."
  [event-channel state-agent options]
  (let [handler (wrap-middleware (app-routes event-channel state-agent))]
    (if-let [wrapper (:ring/wrapper options)]
      (wrapper handler)
      handler)))



;;;;; WEB SERVER ;;;;;

(defrecord WebServer
  [options event-channel state-agent ^Server server]

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
      (let [handler (app-handler event-channel state-agent options)
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
  (WebServer. options nil nil nil))
