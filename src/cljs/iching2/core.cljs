(ns iching2.core
  (:require [cljsjs.material-ui]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [pushy.core :as pushy]
            [iching2.views :as views]
            [iching2.book :as book]
            [iching2.navigation :as navigation]
            [iching2.routes :as routes]))

(defonce state
  (atom {:loaded false}))

(set! routes/state state)

;; -------------------------
;; Routes
(def routes
  {:route/roll  #'views/home-page
   :route/root  #'views/home-page
   :route/about #'views/about-page})


(defn current-page []
  (let [route-key (session/get :current-page :route/root)
        view-fn (get routes route-key)]
    [:div [view-fn state]]))

;; -------------------------
;; History

(defonce make-history
  (set! navigation/history
        (pushy/pushy secretary/dispatch! identity)))
;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (pushy/start! navigation/history)
  (mount-root)
  (book/fetch-book state))
