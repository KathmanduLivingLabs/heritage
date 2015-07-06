(ns heritage.info
   (:require-macros [cljs.core.async.macros :refer [go]])
   (:require [om.core :as om :include-macros true]
            [hatti.ona.forms :as f]
             [sablono.core :as html :refer-macros [html]]))

(defmethod volunteer-dataview :default 
  [app-state owner opts]
  (reify
    om/IInitState
    (init-state [_])
    om/IRenderState
    (render-state [_ {:keys [no-geodata?]}]
    
        (html
         [:div.container
       ; (for [record data]
            [:div.img_name
            ; (format-answer img-field
                      ;      (record (:download_url img-field)))
             ;(format-answer name-field
            ;                (record (:name name-field)))
             "Yeah"
             ]
            ;)
          ]
            )
                  )
      )
    )