;
; Copyright © 2017 Symphony Software Foundation
; SPDX-License-Identifier: Apache-2.0
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

(ns clj-symphony.connect
  "Operations related to connections to a Symphony pod.")

(defn- parse-params
  "Parses connection parameters, substituting a :pod-id for the default URLs, if present."
  [params]
  (if-let [pod-id (:pod-id params)]
    (dissoc (merge { :session-auth-url (str "https://" pod-id "-api.symphony.com/sessionauth")
                     :key-auth-url     (str "https://" pod-id "-api.symphony.com/keyauth")
                     :agent-api-url    (str "https://" pod-id ".symphony.com/agent")   ; Note: no -api as of v1.46
                     :pod-api-url      (str "https://" pod-id ".symphony.com/pod") }   ; Note: not -api !
                   params)
            :pod-id)
    params))

(defn connect
  "Connect to a Symphony pod as a given service account user.  Returns a 'connection' object that should be used in all subsequent API calls.

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
        http-client      (org.symphonyoss.client.impl.CustomHttpClient/getClient (first user-cert)   (second user-cert)
                                                                                 (first trust-store) (second trust-store))
        connection       (doto (org.symphonyoss.client.SymphonyClientFactory/getClient org.symphonyoss.client.SymphonyClientFactory$TYPE/V4)
                           (.setDefaultHttpClient http-client))
        auth-client      (org.symphonyoss.symphony.clients.AuthenticationClient. session-auth-url key-auth-url http-client)
        auth             (.authenticate auth-client)
        _                (.init connection http-client auth ^String user-email ^String agent-api-url ^String pod-api-url)]
      connection))

(defn disconnect
  "Disconnect from a Symphony pod.  The connection object should be discarded after this method is called."
  [^org.symphonyoss.client.SymphonyClient c]
  (.shutdown c))

(defn version
  "Returns the version of Symphony at the other end of the given connection, as a String in major.minor.bugfix format."
  [^org.symphonyoss.client.SymphonyClient c]
  (.getVersion (.v1HealthCheckGet (.getAgentSystemApi (.getSymphonyApis c)))))
