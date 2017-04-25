(ns iching2.routes
  (:require [secretary.core :as secretary :include-macros true]
            [reagent.session :as session]))

(defonce state nil)

(secretary/defroute root-path "/" []
  (js/console.log "wtf?!")
  (session/put! :current-page :route/root)
  )

(secretary/defroute roll-path "/roll/:roll" [roll query-params]
  (js/console.log "hay")
  (session/put! :current-page :route/roll)
  (swap! state assoc :navigation
         {:roll     roll
          :question (:question query-params)})
  
  )

(secretary/defroute about-path "/about" []
  (session/put! :current-page :route/about))

