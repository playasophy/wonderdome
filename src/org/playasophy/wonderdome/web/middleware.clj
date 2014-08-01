(ns org.playasophy.wonderdome.web.middleware
  (:require
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [ring.util.response :as r]))


(defn- error-map
  "Converts an exception into a map for handler responses."
  [^Exception ex]
  (let [info {:exception (.getName (class ex))
              :message (.getMessage ex)}
        cause (.getCause ex)]
    (if cause
      (assoc info :cause (.getName (class cause)))
      info)))


(defn wrap-exception-handler
  "Ring middleware to capture application exceptions and return a map of info
  about the error."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (log/error e)
        (-> (r/response (error-map e))
            (r/status 500))))))


(defn wrap-cache-control
  "Ring middleware to add a cache-control header if the response matches
  certain content types."
  [handler cacheable-type? & {:keys [max-age] :or {max-age 300}}]
  (fn [req]
    (let [resp (handler req)]
      (if (cacheable-type? (get-in resp [:headers "Content-Type"]))
        (r/header resp "Cache-Control" (format "public,max-age=%d" max-age))
        resp))))


(defn wrap-request-logger
  "Ring middleware to log information about service requests."
  [handler]
  (fn [{:keys [uri remote-addr request-method] :as request}]
    (let [start (System/currentTimeMillis)
          method (str/upper-case (name request-method))]
      (log/trace remote-addr method uri)
      (let [{:keys [status headers] :as response} (handler request)
            elapsed (- (System/currentTimeMillis) start)
            msg (format "[%s] %s %s %s (%d ms)"
                        status remote-addr method uri elapsed)]
        (if (<= 400 status 599)
          (log/warn msg)
          (log/info msg))
        response))))


(defn wrap-x-forwarded-for
  "Ring middleware to fix the remote address if the request passed through
  proxies on the way to the service.

  This function replaces the request's :remote-addr with the LAST entry in the
  forwarded header. This gives the address of the host which called the service
  endpoint, which may not be the same as the original client."
  [handler]
  (fn [request]
    (if-let [xff (get-in request [:headers "x-forwarded-for"])]
      (let [addrs (str/split xff #"\s*,\s*")]
        (when (> (count addrs) 1)
          (log/info "Multiple request forwards:" (str/join " -> " addrs)))
        (handler (assoc request :remote-addr (last addrs))))
      (handler request))))
