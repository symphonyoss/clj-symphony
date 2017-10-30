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

(ns clj-symphony.user-connection
  "Operations related to user connections and requests for those connections.  In Symphony, a user connection is an explicitly established relationship between users in different pods."
  (:require [clj-symphony.user :as syu]))


(defn userconnectionobj->map
  "Converts a org.symphonyoss.symphony.clients.model.SymUserConnection object into a map."
  [^org.symphonyoss.symphony.clients.model.SymUserConnection uc]
  (if uc
    {
      :user-id            (.getUserId uc)
      :status             (if-let [status (.getStatus uc)]
                            (keyword (str status)))
      :first-request-date (if-let [first-requested-at-epoch (.getFirstRequestedAt uc)]
                            (java.util.Date. first-requested-at-epoch))
      :update-date        (if-let [updated-at-epoch (.getUpdatedAt uc)]
                            (java.util.Date. updated-at-epoch))
      :request-count      (.getRequestCounter uc)
    }))


(def user-connection-states
  "The set of possible user connections states in Symphony, as keywords."
  (set (map #(keyword (str %)) (org.symphonyoss.symphony.clients.model.SymUserConnection$Status/values))))


(defn user-connectionsobjs
  "Returns all org.symphonyoss.symphony.clients.model.SymUserConnection objects for the authenticated user."
  [^org.symphonyoss.client.SymphonyClient c]
  (.getAllConnections (.getConnectionsClient c)))


(defn user-connections
  "Returns all user connections for the authenticated user, as a lazy sequence of maps (see clj-symphony.user-connection/userconnectionobj->map for details)."
  [c]
  (map userconnectionobj->map (user-connectionsobjs c)))


(defn accepted-requestsobjs
  "Returns all accepted user connection requests as org.symphonyoss.symphony.clients.model.SymUserConnection objects for the authenticated user."
  [^org.symphonyoss.client.SymphonyClient c]
  (.getAcceptedRequests (.getConnectionsClient c)))


(defn accepted-requests
  "Returns all accepted user connection requests for the authenticated user, as a lazy sequence of maps (see clj-symphony.user-connection/userconnectionobj->map for details)."
  [c]
  (map userconnectionobj->map (accepted-requestsobjs c)))


(defn pending-requestsobjs
  "Returns all pending user connection requests as org.symphonyoss.symphony.clients.model.SymUserConnection objects for the authenticated user."
  [^org.symphonyoss.client.SymphonyClient c]
  (.getPendingRequests (.getConnectionsClient c)))


(defn pending-requests
  "Returns all pending user connection requests for the authenticated user, as a lazy sequence of maps (see clj-symphony.user-connection/userconnectionobj->map for details)."
  [c]
  (map userconnectionobj->map (pending-requestsobjs c)))


(defn rejected-requestsobjs
  "Returns all rejected user connection requests as org.symphonyoss.symphony.clients.model.SymUserConnection objects for the authenticated user."
  [^org.symphonyoss.client.SymphonyClient c]
  (.getRejectedRequests (.getConnectionsClient c)))


(defn rejected-requests
  "Returns all rejected user connection requests for the authenticated user, as a lazy sequence of maps (see clj-symphony.user-connection/userconnectionobj->map for details)."
  [c]
  (map userconnectionobj->map (rejected-requestsobjs c)))


(defn incoming-requestsobjs
  "Returns all incoming user connection requests as org.symphonyoss.symphony.clients.model.SymUserConnection objects for the authenticated user."
  [^org.symphonyoss.client.SymphonyClient c]
  (.getIncomingRequests (.getConnectionsClient c)))


(defn incoming-requests
  "Returns all incoming user connection requests for the authenticated user, as a lazy sequence of maps (see clj-symphony.user-connection/userconnectionobj->map for details)."
  [c]
  (map userconnectionobj->map (incoming-requestsobjs c)))


(defn- ^org.symphonyoss.symphony.clients.model.SymUserConnectionRequest build-connection-requestobj
  [u]
  (org.symphonyoss.symphony.clients.model.SymUserConnectionRequest.
    (doto (org.symphonyoss.symphony.clients.model.SymUserConnection.)
      (.setUserId (syu/user-id u)))))


(defn send-connection-request!
  "Sends a connection request to the given user."
  [^org.symphonyoss.client.SymphonyClient c u]
  (.sendConnectionRequest (.getConnectionsClient c) (build-connection-requestobj u))
  nil)


(defn accept-connection-request!
  "Accepts a connection request from the given user."
  [^org.symphonyoss.client.SymphonyClient c u]
  (.acceptConnectionRequest (.getConnectionsClient c) (build-connection-requestobj u))
  nil)


(defn reject-connection-request!
  "Rejects a connection request from the given user, and returns a SymUserConnection object."
  [^org.symphonyoss.client.SymphonyClient c u]
  (.rejectConnectionRequest (.getConnectionsClient c) (build-connection-requestobj u))
  nil)


(defn accept-all-connection-requests!
  "Convenience method that unconditionally accepts all incoming user connection requests, returning the number accepted."
  [c]
  (let [incoming-requests (incoming-requests c)]
    (doall (map (partial accept-connection-request! c) incoming-requests))
    (count incoming-requests)))


(defn reject-all-connection-requests!
  "Convenience method that unconditionally rejects all incoming user connection requests, returning the number rejected."
  [c]
  (let [incoming-requests (incoming-requests c)]
    (doall (map (partial reject-connection-request! c) incoming-requests))
    (count incoming-requests)))
