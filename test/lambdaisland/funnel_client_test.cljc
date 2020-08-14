(ns lambdaisland.funnel-client-test
  (:require [lambdaisland.funnel-client :as funnel-client]
            [lambdaisland.funnel-client.test-util :refer [will]]
            [clojure.test :refer [deftest testing is are use-fixtures run-tests join-fixtures]]))

(deftest smoke-test
  (let [conn1-msgs (atom [])
        conn2-msgs (atom [])]
    (with-open [conn1 (funnel-client/connect {:whoami {:id :abc :xtra 123} :on-message (fn [_ msg] (swap! conn2-msgs conj msg))})
                conn2 (funnel-client/connect {:whoami {:id :def :xtra 456} :on-message (fn [_ msg] (swap! conn2-msgs conj msg))})]
      (funnel-client/send conn1 {:funnel/query {:id :def}})
      (funnel-client/send conn1 {:funnel/broadcast {:id :def}
                                 :hello :world})
      (will (= #{{:funnel/clients [{:id :def :xtra 456}]}
                 {:funnel/broadcast {:id :def}
                  :hello :world
                  :funnel/whoami {:id :abc :xtra 123}}}
               (set @conn2-msgs))))))

(comment
  (require '[kaocha.repl :as r])
  (r/run))
