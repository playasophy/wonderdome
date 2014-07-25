(ns org.playasophy.wonderdome.web.app
  (:require
    [clojure.string :as str]
    (compojure
      [core :refer [ANY GET POST routes]]
      [route :refer [not-found]])
    [com.stuartsierra.component :as component]
    [hiccup.core :as hiccup]
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
      (r/response "Hello World"))
    (ANY "/" []
      (method-not-allowed :get))

    (GET "/about" []
      (render view/about))
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
      (wrap-resource "/public")
      wrap-content-type
      wrap-not-modified
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
                     :host "localhost"
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
