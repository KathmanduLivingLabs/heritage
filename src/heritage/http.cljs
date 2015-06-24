(ns heritage.http
  (:require [cljs.core.async :refer [<! put!]]
            [cljs-http.client :as http]
            [cljs-http.core :as http-core]))

(def raw-request
  "An almost 'batteries-included' request, similar to cljs-http.client/request.
   Contains everything except response decoding."
  (-> http-core/request
      http/wrap-accept
      http/wrap-form-params
      http/wrap-content-type
      http/wrap-json-params
      http/wrap-edn-params
      http/wrap-query-params
      http/wrap-basic-auth
      http/wrap-oauth
      http/wrap-android-cors-bugfix
      http/wrap-method
      http/wrap-url))
(defn raw-get
  [url & [req]]
  "Returns raw get output given a url, without decoding json/edn/transit output."
  (raw-request (merge req {:method :get :url url})))
