(ns lambdaisland.funnel-client.test-util
  (:require [clojure.test :as t])
  #?(:cljs (:require-macros [lambdaisland.funnel-client.test-util :refer [will]])))

#?(:clj
   (defmacro will
     "Variant of [[clojure.test/is]] that gives the predicate a bit of time to become true."
     [expected]
     `(loop [i# 0]
        (if (and (not ~expected) (< i# 10))
          (do
            (Thread/sleep 10)
            (recur (inc i#)))
          (t/is ~expected)))))
