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

(ns clj-symphony.user)


(defn userobj->map
  "Converts a SymUser object into map."
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


(defn get-userobjs
  "Returns all users in the pod, as SymUser objects.  Will throw an exception if the authenticated connection user doesn't have sufficient permissions."
  [^org.symphonyoss.client.SymphonyClient connection]
  (.getAllUsers (.getUsersClient connection)))


(defn get-users
  "Returns all users in the pod.  Will throw an exception if the authenticated connection user doesn't have sufficient permissions."
  [connection]
  (map userobj->map (get-userobjs connection)))


(defmulti get-userobj
  "Returns a SymUser object for the given user identifier, or the authenticated connection user if a user id is not provided.
User can be specified either as a user id (Long) or an email address (String).
Returns nil if the user doesn't exist.

Note: providing a user identifier requires calls to the server."
  (fn
    ([connection]                 :current-user)
    ([connection user-identifier] (type user-identifier))))

(defmethod get-userobj :current-user
  [^org.symphonyoss.client.SymphonyClient connection]
  (.getLocalUser connection))

(defmethod get-userobj nil
  [connection user-id]
  nil)

(defmethod get-userobj org.symphonyoss.symphony.clients.model.SymUser
  [connection user]
  user)

(defmethod get-userobj Long
  [^org.symphonyoss.client.SymphonyClient connection ^Long user-id]
  (try
    (.getUserFromId (.getUsersClient connection) user-id)
    (catch org.symphonyoss.exceptions.UserNotFoundException unfe
      nil)))

(defmethod get-userobj String
  [^org.symphonyoss.client.SymphonyClient connection ^String user-email-address]
  (try
    (.getUserFromEmail (.getUsersClient connection) user-email-address)
    (catch org.symphonyoss.exceptions.UserNotFoundException unfe
      nil)))

(defmethod get-userobj java.util.Map
  [connection {:keys [user-id]}]
  (if user-id
    (get-userobj connection user-id)))


(defn get-user
  "Returns a user for the given user identifier, or the authenticated connection user if a user id is not provided."
  ([connection]   (userobj->map (get-userobj connection)))
  ([connection u] (userobj->map (get-userobj connection u))))


(defn get-userobj-by-username
  "Returns a SymUser object for the given a username, or nil if the user doesn't exist."
  [^org.symphonyoss.client.SymphonyClient connection ^String username]
  (.getUserFromName (.getUsersClient connection) username))


(defn get-user-by-username
  "Returns a user for the given a username, or nil if the user doesn't exist."
  [connection username]
  (userobj->map (get-userobj-by-username connection username)))


(def presence-states
  "The set of possible presence states in Symphony, as keywords."
  (set (map #(keyword (str %)) (org.symphonyoss.symphony.pod.model.Presence$CategoryEnum/values))))


(defn presences
  "Returns the presence status of all users visible to the authenticated connection user, as a seq of maps with keys :user-id (value is a long) and :presence (value is a keyword)."
  [^org.symphonyoss.client.SymphonyClient connection]
  (map #(hash-map :user-id   (.getUid ^org.symphonyoss.symphony.pod.model.UserPresence %)
                  :presence (keyword (str (.getCategory ^org.symphonyoss.symphony.pod.model.UserPresence %))))
       (.getAllUserPresence (.getPresenceService connection))))


(defmulti presence
  "Returns the presence status of a single user, as a keyword.  If no user identifier is provided, returns the presence status of the authenticated connection user."
  (fn
    ([connection]                 :current-user)
    ([connection user-identifier] (type user-identifier))))

(defmethod presence :current-user
  [connection]
  (presence connection (get-user connection)))

(defmethod presence nil
  [connection user-id]
  nil)

(defmethod presence org.symphonyoss.symphony.clients.model.SymUser
  [connection ^org.symphonyoss.symphony.clients.model.SymUser user]
  (presence connection (.getId user)))

(defmethod presence Long
  [^org.symphonyoss.client.SymphonyClient connection ^Long user-id]
  (keyword (str (.getCategory (.getUserPresence (.getPresenceService connection) user-id)))))

(defmethod presence String
  [connection ^String user-email-address]
  (presence connection (get-userobj connection user-email-address)))

(defmethod presence java.util.Map
  [connection {:keys [user-id]}]
  (if user-id
    (presence connection user-id)))


(defmulti set-presence!
  "Sets the presence status of the given user, or the authenticated connection user if not provided.  new-presence must be one of presence-states."
  (fn
    ([connection new-presence]                 :current-user)
    ([connection user-identifier new-presence] (type user-identifier))))

(defmethod set-presence! :current-user
  [connection new-presence]
  (set-presence! connection (get-user connection) new-presence))

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
  (set-presence! connection (get-userobj connection user-email-address) new-presence))

(defmethod set-presence! java.util.Map
  [connection {:keys [user-id]} new-presence]
  (if user-id
    (set-presence! connection user-id new-presence)))
