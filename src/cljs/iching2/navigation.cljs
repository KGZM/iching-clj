(ns iching2.navigation
  (:require [pushy.core :as pushy]))

(defonce history nil)

(defn goto [path]
  (pushy/set-token! history path))


(defn change [path]
  (pushy/replace-token! history path))
