(ns heritage.details
   (:require-macros [cljs.core.async.macros :refer [go]])
   (:require [om.core :as om :include-macros true]
             [sablono.core :as html :refer-macros [html]]
             [hatti.views :refer [details-page]]))

(defmethod details-page "heritage-details"
  [{:keys [dataset-info]} owner]
  "Om component for the whole details page."
  (om/component
   (html
    [:div.container "The page is under development"])))
