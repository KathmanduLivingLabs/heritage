(ns heritage.volunteer
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! chan mult tap put! timeout]]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [hatti.ona.forms :as f]
            [hatti.utils :refer [json->cljs format last-url-param]]))

(defn empty-app-state
  "An initial, empty, app-state, which can be modified to change dataviews."
  []
  (atom
   {:views [:about]
    :about-page {}}))

(def app-state (empty-app-state))
(defn transact!
  [app-state]
  (if (satisfies? om/ITransact app-state) om/transact! swap!))

(defn transact-app-state!
  [app-state ks transact-fn]
  ((transact! app-state) app-state #(update-in % ks transact-fn)))