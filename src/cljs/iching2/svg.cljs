(ns iching2.svg
  (:require [clojure.string :as string]))

(def simple-ops
  #{:m :M :l :L})

(defn process-simple-op
  [[op x y & tail]]
  [(str (name op) x "," y) tail])

(defn process-op [ops]
  (let [op     (first ops)]
    (cond
      (simple-ops op) (process-simple-op ops))))

(defn d [& ops]
  (loop [ops    ops
         output []]
    (let [[current tail] (process-op ops)
          output         (conj output current)]
      (if (not-empty tail)
        (recur tail output)
        (string/join " " output)))))

(defn hexagram [formula]
  (let [yin          #{6 8}
        yang         #{7 9}
        old          #{6 9}
        stroke-width 8
        ;;stroke       "#000"
        ;; spacing      20
        spacing      (/ 100 6)
        height       (- 100
                        (/ spacing 2))]
    [:figure {:class "hexagram-container"}
     (into
      [:svg {:class   "hexagram"
             ;; :preserveAspectRatio "none" 
             :viewBox "0 0 100 100"}]
      (for [[index line] (map-indexed vector formula)]
        [:g {:class (string/join
                     " "
                     ["line"
                      (str "line-" index)
                      (when (yin line) "line-yin")
                      (when (yang line) "line-yin")
                      (when (old line) "line-old")])}
         (when (yin line)
           [:path {:strokeWidth     stroke-width
                   :strokeDasharray "40,20,40"
                   :d               (d :M 0 (- height (* spacing index))
                                       :l 100 0)}])
         (when (yang line)
           [:path {:strokeWidth stroke-width
                   :d           (d :M 0 (- height (* spacing index))
                                   :l 100 0)}])
         (when (and false (old line) (yin line))
           [:path {:strokeWidth 2
                   :d  (->> (for [x [(- stroke-width) stroke-width]
                                  y [(- stroke-width) stroke-width]]
                              [:l x y
                               :m (- x) (- y)])
                            (flatten)
                            (apply d :M  50 (- height (* index spacing))))}])
         (when (and false (old line) (yang line))
           [:circle {:cx          50
                     :cy          (- height (* index spacing))
                     :r           stroke-width
                     :fill        "transparent"
                     :strokeWidth 2}])]))]))
