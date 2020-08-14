(ns lambdaisland.funnel-client
  (:require [clojure.string :as str]
            [cognitect.transit :as transit]
            [goog.object :as gobj]
            [lambdaisland.funnel-client.random-id :as random-id]
            [lambdaisland.funnel-client.websocket :as websocket]
            [lambdaisland.glogi :as log]
            [platform :as platform])
  (:require-macros [lambdaisland.funnel-client.macros :refer [working-directory]])
  (:import [goog.net WebSocket]))

(defprotocol Socket
  (send [socket message]))

(goog-define FUNNEL_URI "")

(def platform-type (re-find #"\w+" (str/lower-case (.-name platform))))

(def client-id (str platform-type "-" (random-id/rand-id)))

(def whoami (atom {:id                client-id
                   :has-dom?          (exists? js/document)
                   :platform-type     platform-type
                   :platform          (.-description platform)
                   :working-directory (working-directory)}))

(def transit-read-handlers (atom {}))
(def transit-write-handlers (atom {}))

(defn funnel-uri []
  (if (empty? FUNNEL_URI)
    (let [protocol (if (exists? js/location) js/location.protocol "http")
          hostname (if (exists? js/location) js/location.hostname "localhost")
          https? (str/starts-with? protocol "https")]
      (str (if https? "wss" "ws") "://" hostname ":" (if https? "44221" "44220")))
    FUNNEL_URI))

(defn- noop [& _])

(defn- atom? [x]
  (satisfies? IAtom x))

(defn connect
  [{:keys [uri on-open on-message on-error on-close transit-reader transit-writer whoami]
    :or {uri (funnel-uri)
         on-open noop
         on-message noop
         on-error noop
         on-close noop
         transit-reader (transit/reader :json {:handlers @transit-read-handlers})
         transit-writer (transit/writer :json {:handlers @transit-write-handlers})
         whoami whoami}
    :as opts}]
  (log/info :connecting uri)
  (let [ws (websocket/ensure-websocket #(goog.net.WebSocket. true))
        send! #(websocket/send! ws (transit/write transit-writer %))]
    (assert ws)
    (websocket/register-handlers ws
                                 {:open
                                  (fn [e]
                                    (log/debug :websocket/open (into {} (map (juxt keyword #(gobj/get e %))) (js/Object.keys e)))
                                    (send! {:type ::connected
                                            :funnel/whoami (if (map? whoami) whoami @whoami)})
                                    (when (atom? whoami)
                                      (add-watch whoami
                                                 ::resend-whoami
                                                 (fn [_ _ _ new-whoami]
                                                   (send! {:type ::whoami-watch
                                                           :funnel/whoami new-whoami}))))
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
        (send! message))
      IMeta
      (-meta [_]
        opts))
    (websocket/open! ws uri)
    ws))

(defn disconnect! [socket]
  (when socket
    (log/info :disconnecting socket)
    (websocket/close! socket)))
