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

(ns clj-symphony.user
  "Operations related to users.  A user can be either a human or a service account (bot).")


(defn userobj->map
  "Converts a SymUser object into a map."
  [^org.symphonyoss.symphony.clients.model.SymUser user]
  (if user
    {
      :user-id       (.getId           user)
      :username      (.getUsername     user)
      :email-address (.getEmailAddress user)
      :title         (.getTitle        user)
      :first-name    (.getFirstName    user)
      :last-name     (.getLastName     user)
      :display-name  (.getDisplayName  user)
      :company       (.getCompany      user)
      :location      (.getLocation     user)
      :avatars       (map #(hash-map :size (.getSize ^org.symphonyoss.symphony.clients.model.SymAvatar %)
                                     :url  (.getUrl  ^org.symphonyoss.symphony.clients.model.SymAvatar %))
                          (.getAvatars user))
    }))


(defmulti user-id
  "Returns the id (a Long) of the given user."
  {:arglists '([user])}
  (fn ([user] (type user))))

(defmethod user-id nil
  [user]
  nil)

(defmethod user-id Long
  [^Long user-id]
  user-id)

(defmethod user-id java.util.Map
  [{:keys [user-id]}]
  user-id)

(defmethod user-id org.symphonyoss.symphony.clients.model.SymUser
  [^org.symphonyoss.symphony.clients.model.SymUser user]
  (.getId user))


(defn userobjs
  "Returns all users in the pod, as org.symphonyoss.symphony.clients.model.SymUser objects.  Will throw an exception if the authenticated connection user doesn't have sufficient permissions."
  [^org.symphonyoss.client.SymphonyClient connection]
  (.getAllUsers (.getUsersClient connection)))


(defn users
  "Returns all users in the pod.  Will throw an exception if the authenticated connection user doesn't have sufficient permissions."
  [connection]
  (map userobj->map (userobjs connection)))


(defmulti userobj
  "Returns a org.symphonyoss.symphony.clients.model.SymUser object for the given user identifier, or the authenticated connection user if a user id is not provided. User can be specified either as a user id (Long) or an email address (String). Returns nil if the user doesn't exist.

Note: providing a user identifier requires calls to the server."
  {:arglists '([connection]
               [connection user])}
  (fn
    ([connection]      :current-user)
    ([connection user] (type user))))

(defmethod userobj :current-user
  [^org.symphonyoss.client.SymphonyClient connection]
  (.getLocalUser connection))

(defmethod userobj nil
  [connection user-id]
  nil)

(defmethod userobj org.symphonyoss.symphony.clients.model.SymUser
  [connection user]
  user)

(defmethod userobj Long
  [^org.symphonyoss.client.SymphonyClient connection ^Long user-id]
  (try
    (.getUserFromId (.getUsersClient connection) user-id)
    (catch org.symphonyoss.client.exceptions.UserNotFoundException unfe
      nil)))

(defmethod userobj String
  [^org.symphonyoss.client.SymphonyClient connection ^String email-address]
  (try
    (.getUserFromEmail (.getUsersClient connection) email-address)
    (catch org.symphonyoss.client.exceptions.UserNotFoundException unfe
      nil)))

(defmethod userobj java.util.Map
  [connection {:keys [user-id]}]
  (if user-id
    (userobj connection user-id)))


(defn user
  "Returns a user for the given user identifier, or the authenticated connection user if a user id is not provided."
  ([connection]                 (userobj->map (userobj connection)))
  ([connection user-identifier] (userobj->map (userobj connection user-identifier))))


(defn userobj-by-username
  "Returns a org.symphonyoss.symphony.clients.model.SymUser object for the given username, or nil if the user doesn't exist."
  [^org.symphonyoss.client.SymphonyClient connection ^String username]
  (.getUserFromName (.getUsersClient connection) username))


(defn user-by-username
  "Returns a user for the given username, or nil if the user doesn't exist."
  [connection username]
  (userobj->map (userobj-by-username connection username)))


(defn same-pod?
  "Returns true if the given user is in the same pod as the authenticated connection user, or nil if the user doesn't exist."
  [connection user-identifier]
  (let [me (user connection)]
    (if-let [them (user connection user-identifier)]
      (= (:company me) (:company them)))))


(defn cross-pod?
  "Returns true if the given user is in a different pod to the authenticated connection user, or nil if the user doesn't exist."
  [connection user-identifier]
  (let [me (user connection)]
    (if-let [them (user connection user-identifier)]
      (not= (:company me) (:company them)))))


(def presence-states
  "The set of possible presence states in Symphony, as keywords."
  (set (map #(keyword (str %)) (org.symphonyoss.symphony.pod.model.Presence$CategoryEnum/values))))


(comment   ; Frank was asked to remove this from SJC circa v1.0.2...
(defn presences
  "Returns the presence status of all users visible to the authenticated connection user, as a seq of maps with keys :user-id (value is a long) and :presence (value is a keyword)."
  [^org.symphonyoss.client.SymphonyClient connection]
  (map #(hash-map :user-id   (.getUid ^org.symphonyoss.symphony.pod.model.UserPresence %)
                  :presence (keyword (str (.getCategory ^org.symphonyoss.symphony.pod.model.UserPresence %))))
       (.getAllUserPresence (.getPresenceService connection))))
)

(defmulti presence
  "Returns the presence status of a single user, as a keyword.  If no user identifier is provided, returns the presence status of the authenticated connection user."
  {:arglists '([connection]
               [connection user-identifier])}
  (fn
    ([connection]                 :current-user)
    ([connection user-identifier] (type user-identifier))))

(defmethod presence :current-user
  [connection]
  (presence connection (user connection)))

(defmethod presence nil
  [connection user-id]
  nil)

(defmethod presence org.symphonyoss.symphony.clients.model.SymUser
  [connection ^org.symphonyoss.symphony.clients.model.SymUser user]
  (presence connection (.getId user)))

(defmethod presence Long
  [^org.symphonyoss.client.SymphonyClient connection ^Long user-id]
  (keyword (str (.getCategory (.getUserPresence (.getPresenceClient connection) user-id)))))

(defmethod presence String
  [connection ^String user-email-address]
  (presence connection (userobj connection user-email-address)))

(defmethod presence java.util.Map
  [connection {:keys [user-id]}]
  (if user-id
    (presence connection user-id)))


(defmulti set-presence!
  "Sets the presence status of the given user, or the authenticated connection user if not provided.  new-presence must be one of presence-states."
  {:arglists '([connection new-presence]
               [connection user-identifier new-presence])}
  (fn
    ([connection new-presence]                 :current-user)
    ([connection user-identifier new-presence] (type user-identifier))))

(defmethod set-presence! :current-user
  [connection new-presence]
  (set-presence! connection (user connection) new-presence))

(defmethod set-presence! org.symphonyoss.symphony.clients.model.SymUser
  [connection ^org.symphonyoss.symphony.clients.model.SymUser user new-presence]
  (set-presence! connection (.getId user) new-presence))

(defmethod set-presence! Long
  [^org.symphonyoss.client.SymphonyClient connection ^Long user-id new-presence]
  (let [presence-enum (org.symphonyoss.symphony.pod.model.Presence$CategoryEnum/valueOf (name new-presence))
        user-presence (doto (org.symphonyoss.symphony.pod.model.Presence.)
                            (.setCategory presence-enum))]
    (.setUserPresence (.getPresenceClient connection) user-id user-presence)
    nil))

(defmethod set-presence! String
  [connection ^String user-email-address new-presence]
  (set-presence! connection (userobj connection user-email-address) new-presence))

(defmethod set-presence! java.util.Map
  [connection {:keys [user-id]} new-presence]
  (if user-id
    (set-presence! connection user-id new-presence)))
