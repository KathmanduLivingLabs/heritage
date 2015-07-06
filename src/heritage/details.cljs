(ns heritage.details
   (:require-macros [cljs.core.async.macros :refer [go]])
   (:require [om.core :as om :include-macros true]
             [cljs.core.async :refer [put!]]
             [sablono.core :as html :refer-macros [html]]
            [hatti.ona.forms :refer [format-answer]]
    [secretary.core :as secretary :refer-macros [defroute]]
            ;; HATTI Reqs
            [hatti.ona.forms :as f]
            [heritage.volunteer :as volunteer]
            [heritage.details :refer [about-page]]
            [heritage.info]
            [hatti.utils :refer [click-fn pluralize-number]]))


(def dataview-map
  {:about {:view :about
          :component about-page}
 })

(defmethod dataview-actions :default
  [cursor owner]
  (om/component (html nil)))

(defmethod volunteer-dataview :default
  [app-state owner opts]
  (reify
    om/IInitState
    (init-state [_]
      (let [form (om/get-shared owner :flat-form)
            geopoints? (-> app-state :dataset-info :instances_with_geopoints)
            has-geodata? (if (some f/geopoint? form)
                           geopoints?
                           (some f/geofield? form))]
        (when-not has-geodata?
          (om/update! app-state [:views :selected] :table))
        {:no-geodata? (not has-geodata?)}))
    om/IRenderState
    (render-state [_ {:keys [no-geodata?]}]
      (let [active-view (-> app-state :views :about)
            view->display #(if (= active-view %) "block" "none")
                        dataviews (map dataview-map (-> app-state :views))
]
        (html
         [:div.tab-container.dataset-tabs
          [:div.tab-bar
           (map dv->link dataviews)
         
          (for [{:keys [component view]} dataviews]
            [:div {:class (str "tab-page " (name view) "-page")
                   :style {:display (view->display view)}}
             [:div.tab-content {:id (str "tab-content" view)}
              (om/build component app-state {:opts opts})]])])))))
