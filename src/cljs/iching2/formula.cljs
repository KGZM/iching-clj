(ns iching2.formula)

(def binary-table
  {6 0
   7 1
   8 0
   9 1})

(defn formula->binary [formula]
  (map binary-table formula))

(def changing-set #{6 9})

(defn formula->changing-lines [formula]
  (->> (map-indexed vector formula)
       (filter (comp changing-set second))
       (map first)))

(def change-table
  {9 8
   8 8
   7 7
   6 7})

(defn formula->changed-formula [formula]
  (let [changed (map change-table formula)]
    (when (not= changed formula) changed)))

(defn formula->trigrams [formula]
  (partition 3 (formula->binary formula)))
