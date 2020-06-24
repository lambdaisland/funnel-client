(ns lambdaisland.funnel-client
  (:require [cognitect.transit :as transit]
            [clojure.string :as str]
            [lambdaisland.funnel-client.websocket :as websocket]
            [lambdaisland.glogi :as log]
            [goog.object :as gobj])
  (:import [goog.net WebSocket]))

(defprotocol Socket
  (send [socket message]))

(defn funnel-uri []
  (let [protocol js/location.protocol
        hostname js/location.hostname
        https? (str/starts-with? protocol "https")]
    (str (if https? "wss" "ws") "://" hostname ":" (if https? "44221" "44220"))))

(defn connect
  [{:keys [uri on-open on-message on-error on-close transit-reader transit-writer]
    :or {uri (funnel-uri)
         on-open identity
         on-message identity
         on-error identity
         on-close identity
         transit-reader (transit/reader :json)
         transit-writer (transit/writer :json)}}]
  (log/info :connecting uri)
  (let [ws (websocket/ensure-websocket #(goog.net.WebSocket. true))]
    (websocket/register-handlers ws
                                 {:open
                                  (fn [e]
                                    (log/debug :websocket/open (into {} (map (juxt keyword #(gobj/get e %))) (js/Object.keys e)))
                                    (on-open ws e))

                                  :error
                                  (fn [e]
                                    (log/warn :websocket/error {:callback :onerror :event e})
                                    (on-error ws e))

                                  :message
                                  (fn [e]
                                    (let [msg (transit/read transit-reader (websocket/message-data e))]
                                      (log/finest :websocket/message msg)
                                      (on-message ws msg)))

                                  :close
                                  (fn [e]
                                    (log/info :websocket/close {:callback :onclose :event e})
                                    (on-close ws e))})
    (specify! ws
      Socket
      (send [socket message]
        (assert (websocket/open? socket))
        (log/debug :websocket/send message)
        (websocket/send! socket (transit/write transit-writer message))))
    (websocket/open! ws uri)
    ws))

(defn disconnect! [socket]
  (when socket
    (log/info :disconnecting socket)
    (websocket/close! socket)))
