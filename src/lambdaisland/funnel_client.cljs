(ns lambdaisland.funnel-client
  (:require [cognitect.transit :as transit]
            [clojure.string :as str]
            [lambdaisland.funnel-client.websocket :as websocket]
            [lambdaisland.glogi :as log]
            [platform :as platform]
            [goog.object :as gobj])
  (:require-macros [lambdaisland.funnel-client.macros :refer [working-directory]])
  (:import [goog.net WebSocket]))

(defprotocol Socket
  (send [socket message]))

(goog-define FUNNEL_URI "")

(def client-id (str (random-uuid)))

(def whoami (atom {:id                client-id
                   :has-dom?          (exists? js/document)
                   :platform          (.-description platform)
                   :working-directory (working-directory)}))

(defn funnel-uri []
  (if (empty? FUNNEL_URI)
    (let [protocol (if (exists? js/location) js/location.protocol "http")
          hostname (if (exists? js/location) js/location.hostname "localhost")
          https? (str/starts-with? protocol "https")]
      (str (if https? "wss" "ws") "://" hostname ":" (if https? "44221" "44220")))
    FUNNEL_URL))

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
  (let [ws (websocket/ensure-websocket #(goog.net.WebSocket. true))
        send! #(websocket/send! ws (transit/write transit-writer %))]
    (assert ws)
    (websocket/register-handlers ws
                                 {:open
                                  (fn [e]
                                    (log/debug :websocket/open (into {} (map (juxt keyword #(gobj/get e %))) (js/Object.keys e)))
                                    (send! {:type ::connected
                                            :funnel/whoami @whoami})
                                    (add-watch whoami
                                               ::resend-whoami
                                               (fn [_ _ _ new-whoami]
                                                 (send! {:type ::whoami-watch
                                                         :funnel/whoami new-whoami})))
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
        (send! message)))
    (websocket/open! ws uri)
    ws))

(defn disconnect! [socket]
  (when socket
    (log/info :disconnecting socket)
    (websocket/close! socket)))
