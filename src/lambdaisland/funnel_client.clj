(ns lambdaisland.funnel-client ;; clj
  (:refer-clojure :exclude [send])
  (:require [cognitect.transit :as transit]
            [io.pedestal.log :as log]
            [lambdaisland.funnel-client.random-id :as random-id])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]
           java.net.URI
           org.java_websocket.client.WebSocketClient))

(set! *warn-on-reflection* true)

(def whoami (atom {:id (str "jvm-" (random-id/rand-id))
                   :platform-type "jvm"
                   :working-directory (System/getProperty "user.dir")}))

(defn funnel-uri []
  ;; Using the regular ws connection, rather than wss/ssl, since otherwise we
  ;; need to load up the CA cert that funnel uses, which I haven't figured out
  ;; how to do with WebSocketClient. It's probably ok since this is only
  ;; intended for tooling running locally. The SSL support in funnel is mainly
  ;; there so browsers don't complain when connecting from a https origin.
  (or (System/getProperty "lambdaisland.funnel.uri")
      "ws://localhost:44220"))

(def transit-read-handlers (atom {}))
(def transit-write-handlers (atom {}))

(defn to-transit ^String [value]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json {:handlers @transit-write-handlers})]
    (transit/write writer value)
    (.toString out)))

(defn from-transit [^String transit]
  (let [in (ByteArrayInputStream. (.getBytes transit))
        reader (transit/reader in :json {:handlers @transit-read-handlers})]
    (transit/read reader)))

(defn send [^WebSocketClient ws-client msg]
  (.send ws-client (to-transit msg)))

(defn- noop [& _])

(defn- atom? [x]
  (instance? clojure.lang.IAtom x))

(defn connect ^WebSocketClient
  [{:keys [uri on-open on-message on-error on-close whoami]
    :or {uri (funnel-uri)
         on-open noop
         on-message noop
         on-error noop
         on-close noop
         whoami whoami}
    :as opts}]
  (let [conn (proxy [WebSocketClient clojure.lang.IMeta] [(URI. (str uri))]
               (onOpen [handshake]
                 (log/debug :websocket/open handshake)
                 (send this {:type ::connected
                             :funnel/whoami (if (map? whoami)
                                              whoami
                                              @whoami)})
                 (when (atom? whoami)
                   (add-watch whoami
                              ::resend-whoami
                              (fn [_ _ _ new-whoami]
                                (send this {:type ::whoami-watch
                                            :funnel/whoami new-whoami}))))
                 (on-open this handshake))
               (onError [ex]
                 (log/warn :websocket/error true :exception ex)
                 (on-error this ex))
               (onMessage [raw-message]
                 (let [message (from-transit raw-message)]
                   (log/trace :websocket/message message)
                   (on-message this message)))
               (onClose [code reason remote?]
                 (log/info :websocket/close {:code code :reason reason :remote? remote?})
                 (on-close this {:code code
                                 :reason reason
                                 :remote? remote?}))
               (meta []
                 opts))]
    (when-not (.connectBlocking conn 2 java.util.concurrent.TimeUnit/SECONDS)
      (throw (ex-info "Failed connecting to Funnel, is it running?" {:uri uri})))
    conn))

(defn disconnect! [^WebSocketClient conn]
  (.close conn))

(comment
  (def conn (connect {:uri "wss://localhost:44221" :on-message prn}))
  (def conn2 (connect {:uri "ws://localhost:44220" :on-message prn}))

  (send conn {:funnel/query true
              })

  (meta conn)
  (funnel-uri)
  )
