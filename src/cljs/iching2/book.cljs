(ns iching2.book
  (:require [goog.net.XhrIo :as xhr]
            [clojure.string :as string]
            [reagent.core :as reagent :refer [atom]]
            [iching2.formula :as formula]))

(defonce book (atom nil))

(defn string->list [s]
  (map js/parseInt s))

(defn id-trigram-index [data]
  (let [build-index
        (fn [index trigram]
          (assoc index (js/parseInt (:id trigram)) trigram))]
    (->> data
         :trigrams
         (reduce build-index {})
         (assoc data :trigrams))))

(defn id-hexagram-index [data]
  (let [build-index
        (fn [index hexagram]
          (assoc index (js/parseInt (:id hexagram)) hexagram))]
    (->> data
         :hexagrams
         (reduce build-index {})
         (assoc data :hexagrams))))

(defn binary-trigram-index [data]
  (let [build-index
        (fn [index [id trigram]]
          (assoc index (string->list (:binary trigram)) id))]
    (->> data
         :trigrams
         (reduce build-index {})
         (assoc data :binary->trigram-id))))

(defn binary-hexagram-index [data]
  (let [trigrams (:trigrams data)
        build-index
        (fn [index [id hexagram]]
          (assoc index (->> hexagram
                            :trigrams
                            (map (comp string->list
                                       :binary
                                       trigrams))
                            flatten)
                 id))]
    (->> data
         :hexagrams
         (reduce build-index {})
         (assoc data :binary->hexagram-id))))

(defn ingest-book [state-atom data]
  (when-not (:loaded @state-atom)
    (swap! state-atom assoc :loaded true)
    (reset! book (-> (js->clj data :keywordize-keys true)
                     (id-trigram-index)
                     (id-hexagram-index)
                     (binary-trigram-index)
                     (binary-hexagram-index)))))

(defn fetch-book [state-atom]
  (xhr/send "/data/iching.json"
            (fn [event] (->> event
                             .-target
                             .getResponseText
                             (.parse js/JSON)
                             (ingest-book state-atom)))))

(defn hexagram-by-id [id]
  (get-in @book [:hexagrams id]))

(defn hexagram-from-formula [formula]
  (->> (formula/formula->binary formula)
       (get (:binary->hexagram-id @book))
       (get (:hexagrams @book))))

(defn trigram-by-binary [binary]
  (-> (filter (comp (hash-set (string/join binary))
                    :binary)
              (:trigrams @book))
      (first)))

(defn trigrams-from-formula [formula]
  (map (-> @book :binary->trigram-id)
       (formula/formula->trigrams formula)))
