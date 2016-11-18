(ns iching2.views
  (:require [clojure.string :as string]
            [cljs.pprint]
            [reagent.core :as reagent :refer [atom]]
            [iching2.svg :as svg]
            [iching2.book :as book]
            [iching2.consult :as consult]
            [iching2.formula :as formula]
            [material-ui.core :as ui]
            [material-ui.icons :as ui.icons]))

;;; Utilities

(defn key-with [key-fn value]
  ^{:key (key-fn value)} value)

(defn key-all-with [key-fn values]
  (map key-fn values))

(defn pprint [x]
  (with-out-str (cljs.pprint/pprint x)))

(defn span [contents]
  [:span contents])

(defn linebreaks [s]
  (concat (->> s
               (string/split-lines)
               (map span)
               (interpose [:br]))
          [[:br]]))

;;; Dialog

(defn open-roll-dialog [state]
  (swap! state assoc-in [:roll-dialog :open] true))

(defn close-roll-dialog [state]
  (swap! state assoc-in [:roll-dialog :open] false))

(defn roll-dialog [state]
  (let [question (reagent/cursor state [:question])
        close-fn #(close-roll-dialog state)
        actions  (map reagent/as-element
                      [[ui/FlatButton {:label        "Cancel"
                                       :primary      true
                                       :on-touch-tap close-fn}]
                       [ui/FlatButton {:label        "Roll"
                                       :primary      true
                                       :on-touch-tap #(swap! state assoc-in [:roll] (consult/roll-full))}]])
        ]
    (fn [state]
      [ui/Dialog {:open             (or (-> @state :roll-dialog :open) false)
                  :actions          actions
                  :on-request-close close-fn}
       [:p "What do you want to ask the oracle about?"]
       [ui/TextField {:hint-text    "Life, The Universe, And Everything"
                      :on-change    #(reset! question (-> %1 .-target .-value))
                      :on-key-press #(when (= (-> % .-key) "Enter")
                                       (close-fn)
                                       (swap! state assoc-in [:roll] (consult/roll-full)))}]])))

;;; Layout

(defn layout-frame [state & contents]
  (fn [state & contents]
    [ui/MuiThemeProvider {:mui-theme (ui/getMuiTheme ui/darkBaseTheme)}
     [:div
      [ui/AppBar {:title "I Ching"}]
      [:img.dice-btn {:src "/images/dice-btn.svg" :on-click #(open-roll-dialog state)}]
      (into [:main]
             contents)]]))

;;; Elements

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

(defn changing-line-text [entry]
  (comp linebreaks #(str (get-in entry [:lines % :description]) "\n")))

(defn changing-lines [entry roll]
  [entry-section
   [entry-text-title {:class ["changing"]} "Changing Lines"]
   [entry-text {:class "entry-changing-lines changing"}
    (->> roll
         formula/formula->changing-lines
         (mapcat (changing-line-text entry)))]])

(defn entry-component [roll]
  (fn [roll]
    (let [entry (book/hexagram-from-formula roll)]
      [:div.entry
       [:div.entry-top
        [format-name (:name entry)]
        [svg/hexagram (key-all-with identity roll)]]
       (into [:div.entry-text]
             (->> [[the-image entry]
                   [the-judgement entry]
                   (when (formula/formula->changed-formula roll)
                     [changing-lines entry roll])]
                  (interpose [:div.divider])
                  (filter some?)))])))

(defn result [state roll]
  (fn [state roll]
    (let [roll-1 roll
          roll-2 (formula/formula->changed-formula roll)]
      [:div.result 
       [:div (:question @state)]
       (when roll-1 [entry-component roll-1])
       (when roll-2 [entry-component roll-2])])))

;;; Pages

(defn home-page [state]
  (fn [state]
    (let [roll (:roll @state)]
      [layout-frame state
       (if (:loaded @state)
         [:div
          [roll-dialog state]
          [result state roll]]
         [:div [:h2 "Loading...."]])])))

(defn home-page-1 [state]
  [:h2 "Hold the tape."])

(defn about-page [state]
  [:div [:h2 "About iching2"]
   [:div [:a {:href "/"} "go to the home page"]]])
