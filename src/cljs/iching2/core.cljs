(ns iching2.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [iching2.views :as views]
            [iching2.book :as book]))

(defonce state
  (atom {:loaded false}))

(defn current-page []
  [:div [(session/get :current-page) state]])


;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'views/home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'views/about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root)
  (book/fetch-book state))
