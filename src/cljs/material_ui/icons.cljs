(ns material-ui.icons
  (:require cljsjs.material-ui
            cljsjs.material-ui-svg-icons)
  (:refer-clojure :exclude [List Stepper])
  (:require-macros [material-ui.macros :refer [export-material-ui-icons]]))

(export-material-ui-icons)

