(ns iching2.views
  (:require [clojure.string :as string]
            [cljs.pprint]
            [reagent.core :as reagent :refer [atom]]
            [iching2.svg :as svg]
            [iching2.book :as book]
            [iching2.consult :as consult]
            [iching2.formula :as formula]))

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

(defn do-roll [state]
  (swap! state assoc-in [:roll] (consult/roll-full)))

(def layout-frame
  (mdl-component
   (fn [state & contents]
     (fn [state & contents]
       [:div {:class "mdl-layout mdl-js-layout mdl-layout--fixed-header"}
        [:header {:class  "mdl-layout__header mdl-layout__header--scroll"}
         [:div {:class "mdl-layout__header-row"}
          [:span {:class "mdl-layout-title"} "I Ching"]
          [:div {:class "mdl-layout-spacer"}]
          [:nav {:class "mdl-navigation mdl-layout--large-screen-only"}
           [:button {:on-click #(do-roll state)} "Roll that shit, son."]
           [:a {:class "mdl-navigation__link" :href ""} "About"]]]]
        [:div {:class "mdl-layout__drawer"}
         [:span {:class "mdl-layout-title"} "I Ching"]
         [:nav {:class "mdl-navigation"}
          [:a {:class "mdl-navigation__link" :href ""} "About"]]]
        (into  [:main {:class "mdl-layout__content main-content"}]
               contents)]))))

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
    [layout-frame state
     (if (:loaded @state)
       [:div
        ;; [roll-view state]
        
        [grid
         [cell 4 "placeholding"]
         [cell 4 [entry-component state (:roll @state)]]
         [cell 4 [entry-component state (formula/formula->changed-formula (:roll @state))]]
         ]
        [:div [:a {:href "/about"} "go to about page"]]]
       [:div [:h2 "Loading...."]])]))

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

(defn entry-component [state roll]
  (fn [state roll]
    (let [entry (book/hexagram-from-formula roll)]
        [:div.entry
         [:div.entry-top
          [format-name (:name entry)]
          [svg/hexagram roll]]
         (into [:div.entry-text
                [:div.entry-text-title "The Image"]
                (into [:p] (-> entry :image linebreaks))
                [:div.divider]
                [:div.entry-text-title "The Judgement"]
                (into [:p] (-> entry :judgement linebreaks))]

               (if (formula/formula->changed-formula roll)
                 [[:div.divider]
                  [:div.entry-text-title.changing "Changing Lines"]
                  (->> roll
                       formula/formula->changing-lines
                       (map (comp linebreaks #(get-in entry [:lines % :description])))
                       (into [:p.entry-changing-lines.changing]))]))
         ])))



(defn roll-view [state]
  [:div "Get your roll on."
   [:button {:on-click #(do-roll state)} "Roll that shit, son."]
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
  (->> s
       (string/split-lines)
       (interpose [:br])))
