;
; Copyright © 2016 Symphony Software Foundation
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;

(ns clj-symphony.api
  (:require [clojure.string :as s]))

(defn- parse-params
  "Parses connection parameters, substituting a :pod-id for the default URLs, if present."
  [params]
  (if-let [pod-id (:pod-id params)]
    (dissoc (merge { :session-auth-url (str "https://" pod-id "-api.symphony.com/sessionauth")
                     :key-auth-url     (str "https://" pod-id "-api.symphony.com/keyauth")
                     :agent-api-url    (str "https://" pod-id "-api.symphony.com/agent")
                     :pod-api-url      (str "https://" pod-id "-api.symphony.com/pod") }
                   params)
            :pod-id)
    params))

(defn connect
  "Connect to a Symphony pod as a given service account user.  Returns a 'session' object
  that should be used in all subsequent API calls.

  params is a map containing:
  :pod-id           The id of the pod to connect to - will autopopulate whichever of the 4 URLs aren't provided. (optional - see below)
  :session-auth-url The URL of the session authentication endpoint. (optional - see below)
  :key-auth-url     The URL of the key authentication endpoint. (optional - see below)
  :agent-api-url    The URL of the agent API. (optional - see below)
  :pod-api-url      The URL of the Pod API. (optional - see below)
  :trust-store      A pair of strings containing the path to the trust store and the password of the trust store. (mandatory)
  :user-cert        A pair of strings containing the path to the bot user's certificate and the password of that certificate. (mandatory)
  :user-email       The email address of the bot user. (mandatory)

  Note: if :pod-id is not provided, :session-auth-url and :key-auth-url and :agent-api-url and :pod-api-url are all mandatory."
  [params]
  (let [params           (parse-params params)
        session-auth-url (:session-auth-url params)
        key-auth-url     (:key-auth-url     params)
        agent-api-url    (:agent-api-url    params)
        pod-api-url      (:pod-api-url      params)
        trust-store      (:trust-store      params)
        user-cert        (:user-cert        params)
        user-email       (:user-email       params)
        auth-client      (org.symphonyoss.symphony.clients.AuthorizationClient. session-auth-url key-auth-url)
        _                (.setKeystores auth-client (first trust-store) (second trust-store)
                                                    (first user-cert)   (second user-cert))
        auth             (.authenticate auth-client)
        symph-client     (org.symphonyoss.client.SymphonyClientFactory/getClient org.symphonyoss.client.SymphonyClientFactory$TYPE/BASIC)
        _                (.init symph-client auth user-email agent-api-url pod-api-url)]
      symph-client))

(defn- mapify
  [x]
  (dissoc (bean x) :class))

(defmulti user
  "Returns a user object for the given user, or the authenticated session user if a user id is not provided.
  User can be specified either as a user id (Long) or an email address (String).

  Returns nil if the user doesn't exist.

  Note: providing a user identifier requires calls to the server."
  (fn
    ([session]                 :current-user)
    ([session user-identifier] (type user-identifier))))

(defmethod user :current-user
  [^org.symphonyoss.client.SymphonyClient session]
  (.getLocalUser session))

(defmethod user nil
  [^org.symphonyoss.client.SymphonyClient session user-id]
  nil)

(defmethod user Long
  [^org.symphonyoss.client.SymphonyClient session ^Long user-id]
  (try
    (if-let [u (.getUserFromId (.getUsersClient session) user-id)]
      u)
    (catch org.symphonyoss.symphony.pod.invoker.ApiException ae
      nil)))

(defmethod user String
  [^org.symphonyoss.client.SymphonyClient session ^String user-email-address]
  (.getUserFromEmail (.getUsersClient session) user-email-address))

(defn user-info
  "Returns a map containing information about the given user, or the authenticated session user if a user id is not provided.
  User can be specified either as a user id (Long) or an email address (String).

  Returns nil if the user doesn't exist.

  Note: providing a user identifier requires calls to the server."
  ([session]                 (if-let [u (user session)]                 (mapify u)))
  ([session user-identifier] (if-let [u (user session user-identifier)] (mapify u))))

(defn user-presence
  "Returns the presence status of the given user, or all users."
  ([^org.symphonyoss.client.SymphonyClient session]               (map mapify (.getAllUserPresence (.getPresenceClient session))))
  ([^org.symphonyoss.client.SymphonyClient session ^Long user-id] (mapify (.getUserPresence (.getPresenceClient session) user-id))))

(defn get-chats
  "Returns a list of chats for the given user, or for the authenticated session user if a user id is not provided."
  ([^org.symphonyoss.client.SymphonyClient session]               (map mapify (.getChats (.getChatService session) (user session))))
  ([^org.symphonyoss.client.SymphonyClient session ^Long user-id] (map mapify (.getChats (.getChatService session) (user session user-id)))))

(defn establish-chat
  "Establishes a chat with the given user."
  [^org.symphonyoss.client.SymphonyClient session user-identifier]
  (let [recipient #{(user session user-identifier)}
        chat      (org.symphonyoss.client.model.Chat.)
        _         (.setLocalUser   chat (user session))
        _         (.setRemoteUsers chat recipient)
        _         (.setStream      chat (.getStream (.getStreamsClient session) ^java.util.Set recipient))]
    chat))

(defn send-message!
  "Sends a message to the given chat.  Both text and MessageML messages are supported."
  [^org.symphonyoss.client.SymphonyClient session ^org.symphonyoss.client.model.Chat chat ^String message]
  (let [msg (org.symphonyoss.symphony.clients.model.SymMessage.)
        _  (.setMessage msg message)]
    (if (.startsWith message "<messageML>")
      (.setFormat  msg org.symphonyoss.symphony.clients.model.SymMessage$Format/MESSAGEML)
      (.setFormat  msg org.symphonyoss.symphony.clients.model.SymMessage$Format/TEXT))
    (.sendMessage (.getMessageService session) chat msg)
    nil))

(defn register-message-listener
  "Registers f, a function of 7 parameters, as a message listener (callback), and returns a handle to that listener
  so that it can be deregistered later on, if needed.  The 7 arguments passed to f are:

     msg-id     - Identifier of the message
     timestamp  - Timestamp the message was sent (as a string in ####???? format)
     stream-id  - Identifier of the stream (chat or room) the message was sent to)
     user-id    - Identifier of the user who sent the message
     msg-format - Format of the message as a keyword (:messageml or :text)
     msg-type   - ####????
     msg        - Text of the message

   The value returned by f (if any) is ignored."
  [^org.symphonyoss.client.SymphonyClient session f]
  (let [listener (reify
                   org.symphonyoss.client.services.MessageListener
                   (onMessage [this msg]
                     (let [msg-t      ^org.symphonyoss.symphony.clients.model.SymMessage msg
                           msg-id     (.getId          msg-t)
                           timestamp  (.getTimestamp   msg-t)
                           stream-id  (.getStreamId    msg-t)
                           user-id    (.getFromUserId  msg-t)
                           msg-format (when-not (nil? (.getFormat msg-t))
                                        (keyword (s/lower-case (str (.getFormat msg-t)))))
                           msg-type   (.getMessageType msg-t)
                           msg-text   (.getMessage     msg-t)]
                      (f msg-id timestamp stream-id user-id msg-format msg-type msg-text))))]
    (.registerMessageListener (.getMessageService session) listener)
    listener))

(defn deregister-message-listener
  "Deregisters a previously-registered message listener.  Once deregistered, a listener should be discarded.
  Returns true if a valid message listener was deregistered, false otherwise."
  [^org.symphonyoss.client.SymphonyClient session ^org.symphonyoss.client.services.MessageListener listener]
  ; #### WARNING: https://github.com/symphonyoss/symphony-java-client/issues/19
  (.removeMessageListener (.getMessageService session) listener))
