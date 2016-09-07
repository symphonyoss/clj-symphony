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

(ns clj-symphony.api)

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

(defn- user-obj
  ([^org.symphonyoss.client.SymphonyClient session]               (.getLocalUser session))
  ([^org.symphonyoss.client.SymphonyClient session ^Long user-id] (.getUserFromId (.getUsersClient session) user-id)))

(defmulti user-info
  "Returns a map containing information about the given user, or the authenticated session user if a user id is not provided.
  User can be specified either as a user id (Long) or an email address (String).

  Returns nil if the user doesn't exist.

  Note: providing a user identifier requires calls to the server."
  (fn
    ([session]                 :current-user)
    ([session user-identifier] (type user-identifier))))

(defmethod user-info :current-user
  [^org.symphonyoss.client.SymphonyClient session]
  (if-let [u (user-obj session)]
    (mapify u)))

(defmethod user-info Long
  [^org.symphonyoss.client.SymphonyClient session ^Long user-id]
  (try
    (if-let [u (user-obj session user-id)]
      (mapify u))
    (catch org.symphonyoss.symphony.pod.invoker.ApiException ae
      nil)))

(defmethod user-info String
  [^org.symphonyoss.client.SymphonyClient session ^String user-email-address]
  (if-let [u (.getUserFromEmail (.getUsersClient session) user-email-address)]
    (mapify u)))

(defn user-presence
  "Returns the presence status of the given user, or all users."
  ([^org.symphonyoss.client.SymphonyClient session]               (map mapify (.getAllUserPresence (.getPresenceClient session))))
  ([^org.symphonyoss.client.SymphonyClient session ^Long user-id] (mapify (.getUserPresence (.getPresenceClient session) user-id))))

(defn get-chats
  "Returns a list of chats for the given user, or for the authenticated session user if a user id is not provided."
  ([^org.symphonyoss.client.SymphonyClient session]               (map mapify (.getChats (.getChatService session) (user-obj session))))
  ([^org.symphonyoss.client.SymphonyClient session ^Long user-id] (map mapify (.getChats (.getChatService session) (user-obj session user-id)))))
