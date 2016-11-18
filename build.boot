(set-env!
 :source-paths #{"sass" "src/cljs" "src/clj"}
 :resource-paths #{"resources"}
 :dependencies '[
                 [adzerk/boot-cljs          "1.7.228-1"  :scope "test"]
                 [adzerk/boot-cljs-repl     "0.3.0"      :scope "test"]
                 [adzerk/boot-reload        "0.4.8"      :scope "test"]
                 [pandeiro/boot-http        "0.7.2"      :scope "test"]
                 [com.cemerick/piggieback   "0.2.1"      :scope "test"]
                 [org.clojure/tools.nrepl   "0.2.12"     :scope "test"]
                 [weasel                    "0.7.0"      :scope "test"]
                 [deraen/boot-sass          "0.2.1"      :scope "test"]
                 [org.slf4j/slf4j-nop       "1.7.13"     :scope "test"]

                 ;; Front-end
                 [org.clojure/clojurescript "1.8.40"]
                 [reagent "0.6.0" :exclusions [org.clojure/tools.reader cljsjs/react]]
                 [reagent-forms "0.5.22"]
                 [reagent-utils "0.1.7"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.1.7" :exclusions [org.clojure/tools.reader]]
                 
                 ;;New frontend
                 [cljsjs/material-ui "0.15.4-0"]
                 ])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]]
 '[deraen.boot-sass      :refer [sass]])

(deftask build []
  (comp (cljs)
        (sass)))

(deftask run []
  (comp (serve :dir "resources"
               :port 3500)
        (watch)
        (cljs-repl)
        (reload)
        (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced}
                 sass {:output-style :compressed})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none :source-map true}
                 reload {:on-jsload 'iching2.core/init!})
  identity)

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
        (run)))

(deftask release []
  (comp (production)
        (build)
        (sift :include #{#"\.out" #"\.cljs\.edn$"} :invert true)
        (target)))
