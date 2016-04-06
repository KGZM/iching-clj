(ns ^:figwheel-no-load iching2.dev
  (:require [iching2.core :as core]
            [weasel.repl :as repl]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :jsload-callback core/mount-root
  )

(repl/connect "ws://localhost:9001")

(core/init!)
