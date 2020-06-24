(ns lambdaisland.funnel-client.examples.reload
  "Simple demo funnel client that waits for a :reload? message and then reloads
  the page. Can be used as a naive way to auto refresh an HTML page like this

     watchexec --watch index.html sh -c \"echo '{:funnel/broadcast [:type :lambdaisland.funnel-client.examples.reload/reload] :reload? true}' | jet --to transit | websocat ws://localhost:44220\"

  "
  (:require [lambdaisland.funnel-client :as funnel]
            [lambdaisland.glogi :as log]
            [lambdaisland.glogi.console :as clog]
            [lambdaisland.funnel-client.websocket :as websocket]))

(log/set-levels {:glogi/root :all})
(clog/install!)

(defn ^:export start []
  (funnel/connect {:on-open
                   (fn [ws _]
                     (funnel/send ws {:funnel/whoami {:type ::reload}}))
                   :on-message
                   (fn [ws msg]
                     (when (:reload? msg)
                       (websocket/close! ws)
                       (js/location.reload)))}))

(comment
  (start)
  )
