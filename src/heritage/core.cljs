(ns ^:figwheel-always heritage.core
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [sablono.core :refer [html]])
  (:require [om.core :as om]
            [cljs.core.async :refer [<! put!]]
            [milia.api.dataset :as api]
            [milia.api.io :as io]
            [milia.utils.remote :as milia-remote]
            [hatti.ona.forms :refer [flatten-form]]
            [hatti.ona.forms :refer [format-answer]]
            [hatti.ona.post-process :refer [integrate-attachments]]
            [hatti.ona.post-process :refer [integrate-attachments!]]
            [hatti.shared :as shared]
            [hatti.utils :refer [json->cljs]]
            [hatti.views :as views]
            [hatti.views.dataview]
            [heritage.volunteer :as volunteer]))

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
                      :map-config {:mapbox-tiles mapbox-tiles}}})))

(defn empty-app-state
  "An initial, empty, app-state, which can be modified to change dataviews."
  []
  (atom
      {:view {:all [:background :methodology :involve :pic]}
   :background "we are happy"
    :methodology "ok this works"
    :involve "these are who got involved"
       :pic-page {:data []
               }}
    ))

(def app-state (empty-app-state))


(go
 (let [data-chan (api/data auth-token dataset-id-n :raw? true)
       form-chan (api/form auth-token dataset-id-n)
       info-chan (api/metadata auth-token dataset-id-n)
       data (-> (<! data-chan) :body json->cljs)
       form (-> (<! form-chan) :body flatten-form)
       info (-> (<! info-chan) :body)]
   (.log js/console (clj->js (integrate-attachments form data)))
     
(defn widget [d owner]
  (reify
    om/IRender
    (render [this]
      (html [:div.container
             [:div.content
             [:div.background (:background d)]
             [:div.methodology (:methodology d)]
             [:div.involve (:involve d)]]
             [:div.picture 
              (for [record data]                 
                  [:div.img-name 
                   (format-answer :img record)
                   ;(format-answer :name record)
                        ]
                   )]]))))
   (om/root widget
            app-state
            {:target (. js/document (getElementById "volunteer"))})))

