(ns iching2.consult)

(defn roll-ndn [dice sides]
  (for [n (range 0 dice)]
    (inc (rand-int sides))))

(def roll-2d6 (partial roll-ndn 2 6))

(def even-table
  {true 2
   false 3})

(defn roll-line []
  (let [lead-dice      (if (every? even? (roll-2d6)) 2 3)
        secondary-dice (->> (roll-2d6)
                            (map (comp even-table even?))
                            (reduce +))]
    (+ lead-dice secondary-dice)))

(defn roll-full []
  (for [n (range 6)]
    (roll-line)))
