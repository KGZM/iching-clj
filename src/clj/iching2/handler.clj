(ns iching2.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [iching2.middleware :refer [wrap-middleware]]
            [environ.core :refer [env]]))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(def loading-page
  (html5
   [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
    (include-css (if (env :dev) "/css/site.css" "/css/site.min.css")
                 "https://code.getmdl.io/1.1.3/material.red-deep_purple.min.css"
                 "https://fonts.googleapis.com/icon?family=Material+Icons"
                 ;; "https://fonts.googleapis.com/css?family=Montserrat:400,700"
                 "https://fonts.googleapis.com/css?family=Open+Sans:400,300,600,700")
    (include-js "https://code.getmdl.io/1.1.3/material.min.js")]
   [:body
    mount-target
    (include-js "/js/app.js")]))


(defroutes routes
  (GET "/" [] loading-page)
  (GET "/about" [] loading-page)
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
