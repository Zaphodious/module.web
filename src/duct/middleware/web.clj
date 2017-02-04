(ns duct.middleware.web
  (:require [compojure.response :as compojure]
            [integrant.core :as ig]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [ring.util.response :as response]))

(defn wrap-hide-errors [handler error-response]
  (fn [request]
    (try
      (handler request)
      (catch Throwable _
        (-> (compojure/render error-response request)
            (response/content-type "text/html")
            (response/status 500))))))

(defn wrap-not-found [handler error-response]
  (fn [request]
    (or (handler request)
        (-> (compojure/render error-response request)
            (response/content-type "text/html")
            (response/status 404)))))

(defn wrap-route-aliases [handler aliases]
  (fn [request]
    (if-let [alias (aliases (:uri request))]
      (handler (assoc request :uri alias))
      (handler request))))

(defmethod ig/init-key ::hide-errors [_ {:keys [response]}]
  #(wrap-hide-errors % response))

(defmethod ig/init-key ::not-found [_ {:keys [response]}]
  #(wrap-not-found % response))

(defmethod ig/init-key ::route-aliases [_ aliases]
  #(wrap-route-aliases % aliases))

(defmethod ig/init-key ::defaults [_ defaults]
  #(wrap-defaults % defaults))

(defmethod ig/init-key ::webjars [_ {:keys [path] :or {path "/assets"}}]
  #(wrap-webjars % path))

(defmethod ig/init-key ::stacktrace [_ options]
  #(wrap-stacktrace % options))
