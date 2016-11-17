(ns iching2.views
  (:require [clojure.string :as string]
            [cljs.pprint]
            [reagent.core :as reagent :refer [atom]]
            [iching2.svg :as svg]
            [iching2.book :as book]
            [iching2.consult :as consult]
            [iching2.formula :as formula]

            [material-ui.core :as ui]
            [material-ui.icons :as ui.icons]
            ))


(declare mdl-component pprint linebreaks entry-component roll-view layout-frame)

(defn mdl-component [component]
  (with-meta component
    {:component-did-mount
     (fn [this]
       (->> (reagent/dom-node this)
            (.upgradeElement js/componentHandler)))
     :component-will-unmount
     (fn [this]
       (->> (reagent/dom-node this)
            (.downgradeElements js/componentHandler)))}))

(def text-field
  (mdl-component
   (fn [opts label value]
     (fn [opts label value]
       [:div.mdl-textfield.mdl-js-textfield.mdl-textfield--floating-label
        [:input (merge {:class "mdl-textfield__input"
                        :type  "text"
                        :on-change #(reset! value (-> % .-target .-value))}
                       opts)]
        [:label {:class "mdl-textfield__label"}
         label]]))))


(defn dialog [state]
  (fn [state]
    (let [open      (reagent/cursor state [:dialog :open])
          component (reagent/cursor state [:dialog :component])]
      (if (and @open @component)
        [:div.dialog-backdrop [@component state]]))))

(defn open-dialog [state component]
  (swap! state assoc-in [:dialog :open] true)
  (swap! state assoc-in [:dialog :component] component))

(defn close-dialog [state]
  (swap! state assoc-in [:dialog :open] false))

(defn roll-dialog [state]
  (let [question (reagent/cursor state [:question])]
    (fn [state]
      [:div.roll-dialog
       [:div {:on-click #(close-dialog state)}
        "Close this shit mang."]
       [ui/TextField {:hint-text    "Pose Your Question"
                      :on-change    #(reset! question (-> %1 .-target .-value))
                      :on-key-press #(when (= (-> % .-key) "Enter")
                                       (close-dialog state)
                                       (swap! state assoc-in [:roll] (consult/roll-full)))
                      }]
       ])))

(defn do-roll [state]
  (open-dialog state roll-dialog))

(def layout-frame
  (mdl-component
   (fn [state & contents]
     (fn [state & contents]
       [ui/MuiThemeProvider {:mui-theme (ui/getMuiTheme ui/darkBaseTheme)}
        [:div {:class "mdl-layout mdl-js-layout mdl-layout--fixed-header"}
         [dialog state]
         [:header {:class  "mdl-layout__header mdl-layout__header--scroll"}
          [:div {:class "mdl-layout__header-row"}
           [:span {:class "mdl-layout-title"} "I Ching"]
           [:div {:class "mdl-layout-spacer"}]
           [:nav {:class "mdl-navigation"}
            [:img.dice-btn {:src "/images/dice-btn.svg" :on-click #(do-roll state)}]]]]
         #_[:div {:class "mdl-layout__drawer"}
            [:span {:class "mdl-layout-title"} "I Ching"]
            [:nav {:class "mdl-navigation"}
             [:a {:class "mdl-navigation__link" :href ""} "About"]]]
         (into  [:main {:class "mdl-layout__content main-content"}]
                contents)]]))))

(defn grid [& contents]
  (fn [& contents]
    (into [:div {:class "mdl-grid"}]
          contents)))

(defn cell [size & contents]
  (fn [size & contents]
    (into [:div {:class (str "mdl-cell mdl-cell--" size "-col")}]
          contents)))



;; ----------
;; Views

(defn home-page [state]
  (fn [state]
    (let [entry-1 (:roll @state)
          entry-2 (-> @state :roll formula/formula->changed-formula not-empty)]
     [layout-frame state
      (if (:loaded @state)
        [:div
         [:div.mdl-grid.mdl-grid--no-spacing
          [:div.mdl-grid.mdl-cell.mdl-cell--4-col.left-grid
           [:div.mdl-cell.mdl-cell--12-col.column "placeholding"]]
          [:div.mdl-grid.mdl-cell.mdl-cell--8-col.right-grid.mdl-cell--8-col-tablet
           [:div.mdl-cell.mdl-cell--12-col.column.mdl-cell--12-col-tablet
            (:question @state)]
           (when entry-1 [:div.mdl-cell.mdl-cell--6-col.column.mdl-cell--4-col-tablet
                          [entry-component state entry-1]])
           (when entry-2 [:div.mdl-cell.mdl-cell--6-col.column.mdl-cell--4-col-tablet
                          [entry-component state entry-2]])]]]
        [:div [:h2 "Loading...."]])])))

(defn home-page-1 [state]
  [:h2 "Hold the tape."])

(defn about-page [state]
  [:div [:h2 "About iching2"]
   [:div [:a {:href "/"} "go to the home page"]]])

;;-----------
;; Components
(defn format-name [name]
  (let [[chinese english] (map string/trim (string/split name "/"))]
    [:div.hexagram-names
     [:div.hexagram-name.hexagram-name-chinese chinese]
     [:div.hexagram-name.hexagram-name-english english]]))

(defn entry-section [& children]
  (into [:div] children))

(defn entry-text
  ([text] (entry-text {} text))
  ([attrs text]
   (into [:p attrs] text)))

(defn entry-text-title
  ([title] (entry-text-title {} title))
  ([attrs title]
   [:div.entry-text-title attrs title]))

(defn the-image [entry]
  [entry-section
   [entry-text-title "The Image"]
   [entry-text (-> entry :image linebreaks)]])

(defn the-judgement [entry]
  [entry-section
   [entry-text-title "The Judgement"]
   [entry-text (-> entry :judgement linebreaks)]])

(defn changing-lines [entry roll]
  [entry-section
   [entry-text-title {:class ["changing"]} "Changing Lines"]
   [entry-text {:class "entry-changing-lines changing"}
    (->> roll
         formula/formula->changing-lines
         (mapv (comp linebreaks #(str (get-in entry [:lines % :description]) "\n"))))]])

(defn entry-component [state roll]
  (fn [state roll]
    (let [entry (book/hexagram-from-formula roll)]
      [:div.entry
       [:div.entry-top
        [format-name (:name entry)]
        [svg/hexagram roll]]
       [:div.entry-text
        (->> [[the-image entry]
              [the-judgement entry]
              (when (formula/formula->changed-formula roll)
                [changing-lines entry roll])]
             (filter some?)
             (interpose [:div.divider]))]])))

(defn roll-view [state]
  [:div "Get your roll on."
   [:img {:src "/images/dice-btn.svg"
          :on-click #(do-roll state)}]
   [:pre "Roll:"
    (-> (:roll @state)
        (pprint))]
   [:pre "Binary:"
    (-> (:roll @state)
        (formula/formula->binary)
        (pprint))]
   [:pre "Trigrams: \n"
    (-> (:roll @state)
        (book/trigrams-from-formula)
        (pprint))]
   [:pre "Hexagrams: \n"
    (-> (:roll @state)
        (book/hexagram-from-formula)
        (pprint))]
   ])

;;----------
;;Utilities

(defn pprint [x]
  (with-out-str (cljs.pprint/pprint x)))

(defn linebreaks [s]
  (concat (->> s
               (string/split-lines)
               (interpose [:br]))
          [[:br]]))
