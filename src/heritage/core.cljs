(ns ^:figwheel-always heritage.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om]
            [cljs.core.async :refer [<! put!]]
            [milia.api.dataset :as api]
            [milia.api.io :as io]
            [milia.utils.remote :as milia-remote]
            [hatti.ona.forms :refer [flatten-form]]
            [hatti.ona.post-process :refer [integrate-attachments]]
            [hatti.shared :as shared]
            [hatti.utils :refer [json->cljs]]
            [hatti.views :as views]
            [hatti.views.dataview]
            ;[cljs-http.client :as http]
            ;[heritage.http :refer [raw-get]]
            ;[heritage.details]
            [heritage.volunteer :as volunteer]
            [ankha.core :as ankha]
         [om.dom :as dom]))

;; CONFIG
(enable-console-print!)
(swap! milia-remote/hosts merge {:ui "localhost:8000"
                                 :data "ona.io"
                                 :ona-api-server-protocol "https"})
(def dataset-id "49501") ;; Cultural Heritage
(def dataset-id-n "54006")
(def mapbox-tiles
  [{:url "http://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png"
    :name "Humanitarian OpenStreetMap Team"
    :attribution "&copy;  <a href=\"http://osm.org/copyright\">
                  OpenStreetMap Contributors.</a>
                  Tiles courtesy of
                  <a href=\"http://hot.openstreetmap.org/\">
                  Humanitarian OpenStreetMap Team</a>."}])
(def auth-token nil)

(defn widget [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/h1 nil (:text data)))))


(def private-fields
  ["surveyor_id" "site_id" "local_contact" "security" "security_comment"
   "meta/instanceID"])

(defn has-value [key value]
  (fn [m]
    (= value (m key))))

(defn remove-private-fields [form]
  (let [r (for [i private-fields]
            (filter  (has-value :full-name i) form))
        flatr (flatten r)]
    (remove (set flatr) form)))

;; define your app data so that it doesn't get over-written on reload
(go
 (let [data-chan (api/data auth-token dataset-id :raw? true)
       form-chan (api/form auth-token dataset-id)
       info-chan (api/metadata auth-token dataset-id)
       data (-> (<! data-chan) :body json->cljs)
       form (-> (<! form-chan) :body flatten-form)
       public-form (remove-private-fields form)
       info (-> (<! info-chan) :body)]
   (shared/update-app-data! shared/app-state data :rerank? true)
   (shared/transact-app-state! shared/app-state [:dataset-info] (fn [_] info))
   (shared/transact-app-state! shared/app-state [:views :all] (fn [_] [:map :table]))
   (integrate-attachments! shared/app-state public-form)
   (om/root views/tabbed-dataview
            shared/app-state
            {:target (. js/document (getElementById "app"))
             :shared {:flat-form public-form
                      :map-config {:mapbox-tiles mapbox-tiles}
                               }})))
(go
 (let [data-chan (api/data auth-token dataset-id-n :raw? true)
       form-chan (api/form auth-token dataset-id-n)
       info-chan (api/metadata auth-token dataset-id-n)
       data (-> (<! data-chan) :body json->cljs)
       form (-> (<! form-chan) :body flatten-form)
       ;public-form (remove-private-fields form)
    info (-> (<! info-chan) :body)]
  ; (shared/update-app-data! shared/app-state data :rerank? true)
   ;(shared/transact-app-state! shared/app-state [:dataset-info] (fn [_] info))
  ; (volunteer/transact-app-state! volunteer/app-state [:views] (fn [_] [:about]))
    
(integrate-attachments form data)
      (println (first (integrate-attachments form data)))
   (om/root widget {:text "hello world"}
            {:target (. js/document (getElementById "volunteer"))

                                               })))
