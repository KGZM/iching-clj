(ns material-ui.core
  (:require [cljsjs.material-ui]
            [cljsjs.material-ui-svg-icons])
  (:refer-clojure :exclude [List Stepper])
  (:require-macros [material-ui.macros :refer [export-material-ui-react-classes]]))

(export-material-ui-react-classes)

(def colors (-> js/MaterialUIStyles
                (aget "colors")))

(def lightBaseTheme (-> js/MaterialUIStyles
                        (aget "lightBaseTheme")))

(def darkBaseTheme (-> js/MaterialUIStyles
                       (aget "darkBaseTheme")))

(def getMuiTheme (-> js/MaterialUIStyles
                     (aget "getMuiTheme")))

(def MuiThemeProvider (-> js/MaterialUIStyles
                          (aget "MuiThemeProvider")
                          (reagent.core/adapt-react-class)))
