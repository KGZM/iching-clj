(ns iching2.views
  (:require [clojure.string :as string]
            [cljs.pprint]
            [reagent.core :as reagent :refer [atom]]
            [iching2.svg :as svg]
            [iching2.book :as book]
            [iching2.consult :as consult]
            [iching2.formula :as formula]
            [iching2.routes :as routes]
            [iching2.navigation :as nav]
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
  (let [question   (reagent/atom "")
        close-fn   #(do (reset! question "")
                        (close-roll-dialog state))
        confirm-fn #(do #_(swap! state merge
                                 {:question @question
                                  :roll     (consult/roll-full)})
                        (nav/goto (routes/roll-path {:roll         (apply str (consult/roll-full))
                                                     :query-params {"date"     (.getTime (new js/Date))
                                                                    "question" @question}}))
                        (close-fn))
        actions    (map reagent/as-element
                        [[ui/FlatButton {:label        "Cancel"
                                         :secondary    true
                                         :on-touch-tap close-fn}]
                         [ui/RaisedButton {:label        "Roll"
                                           :primary      true
                                           :on-touch-tap confirm-fn}]])]
    (fn [state]
      [ui/Dialog {;:title            "Consult The I Ching"
                  :open             (or (-> @state :roll-dialog :open) false)
                  :actions          actions
                  :on-request-close close-fn}
       [ui/Tabs
        [ui/Tab {:label "Simulated Dice"}
         [:br]
         [:p "What do you want to ask the oracle about?"]
         [ui/TextField {:hint-text    "Life, The Universe, And Everything"
                        :on-change    #(reset! question (-> %1 .-target .-value))
                        :on-key-press #(when (= (-> % .-key) "Enter")
                                         (confirm-fn))
                        :full-width   true}]]
        [ui/Tab {:label "Manual"
                 }]
        ]])))

;;; Layout

(defn rgb [r g b]
  (str "rgb(" (apply str (interpose "," [r g b])) ")"))

(defn rgba [r g b a]
  (str "rgba(" (apply str (interpose "," [r g b a])) ")"))

(defn gray [l]
  (rgb l l l))

(def white (rgb 255 255 255))

(def theme
  (-> {:palette   {:primary1Color      (rgb 233 46 60)
                   :primary2Color      (rgb 233 46 60)
                   :primary3Color      (rgb 233 46 60)
                   :accent1Color       (rgb 233 46 60)
                   :accent2Color       (rgb 233 46 60)
                   :accent3Color       (rgb 233 46 60)
                   :canvasColor        (rgb 39 40 45)
                   :textColor          white
                   :alternateTextColor white}
       :inkBar    {:backgroundColor white}
       :textField {:hintColor (gray 150)}
       :dialog    {:bodyColor white}}
      clj->js
      ui/getMuiTheme))

(defn layout-frame [state & contents]
  (fn [state & contents]
    [ui/MuiThemeProvider {:mui-theme theme}
     [:div
      [ui/AppBar {:title "I Ching"}]
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
        [svg/hexagram roll]]
       (into [:div.entry-text]
             (->> [[the-image entry]
                   [the-judgement entry]
                   (when (formula/formula->changed-formula roll)
                     [changing-lines entry roll])]
                  (filter some?)
                  (interpose [:div.divider])
                  (key-all-with identity)))])))

(defn result [state roll]
  (fn [state roll]
    (let [roll-1 roll
          roll-2 (formula/formula->changed-formula roll)
          question (-> @state :navigation :question)]
      [:div
       (when-not (empty? question)
         [:div.result [:div.question [:p "Your topic: " [:strong question]]]])
       [:div.result
        (when roll-1 [entry-component roll-1])
        (when roll-2 [entry-component roll-2])]])))

;;; Pages
(def extract-roll (partial map long))

(defn home-page [state]
  (fn [state]
    (let [roll (-> @state :navigation :roll extract-roll not-empty)]
      [layout-frame state
       (if (:loaded @state)
         [:div
          [roll-dialog state]
          [result state roll]
          [ui/FloatingActionButton
           {:class-name   "main-fab"
            :children     (reagent/as-element [:img.dice-btn {:src "/images/dice-btn.svg"}])
            :on-touch-tap #(open-roll-dialog state)}]]
         [:div [:h2 "Loading...."]])])))

(defn home-page-1 [state]
  [:h2 "Hold the tape."])

(defn about-page [state]
  [:div [:h2 "About iching2"]
   [:div [:a {:href "/"} "go to the home page"]]])
