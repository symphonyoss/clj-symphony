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
  [session user-id]
  nil)

(defmethod user org.symphonyoss.symphony.clients.model.SymUser
  [session user]
  user)

(defmethod user Long
  [^org.symphonyoss.client.SymphonyClient session ^Long user-id]
  (try
    (.getUserFromId (.getUsersClient session) user-id)
    (catch org.symphonyoss.exceptions.UserNotFoundException unfe
      nil)))

(defmethod user String
  [^org.symphonyoss.client.SymphonyClient session ^String user-email-address]
  (try
    (.getUserFromEmail (.getUsersClient session) user-email-address)
    (catch org.symphonyoss.exceptions.UserNotFoundException unfe
      nil)))

(defn properties
  "Returns a user's properties as a map."
  ([session] (properties session (user session)))
  ([session u]
   (if-let [^org.symphonyoss.symphony.clients.model.SymUser user (user session u)]
     {
       :userId       (.getId           user)
       :userName     (.getUsername     user)
       :emailAddress (.getEmailAddress user)
       :title        (.getTitle        user)
       :firstName    (.getFirstName    user)
       :lastName     (.getLastName     user)
       :displayName  (.getDisplayName  user)
       :company      (.getCompany      user)
       :location     (.getLocation     user)
       :avatars      (map #(hash-map :size (.getSize ^org.symphonyoss.symphony.clients.model.SymAvatar %)
                                     :url  (.getUrl  ^org.symphonyoss.symphony.clients.model.SymAvatar %))
                          (.getAvatars user))
     })))

(defmulti presence
  "Returns the presence status of a user."
  (fn
    ([session]                 :current-user)
    ([session user-identifier] (type user-identifier))))

(defmethod presence :current-user
  [session]
  (presence session (user session)))

(defmethod presence nil
  [session user-id]
  nil)

(defmethod presence org.symphonyoss.symphony.clients.model.SymUser
  [session ^org.symphonyoss.symphony.clients.model.SymUser user]
  (presence session (.getId user)))

(defmethod presence Long
  [^org.symphonyoss.client.SymphonyClient session ^Long user-id]
  (keyword (str (.getCategory (.getUserPresence (.getPresenceClient session) user-id)))))

(defmethod presence String
  [session ^String user-email-address]
  (presence session (user session user-email-address)))

(defn all-presence
  "Returns the presence status of all users."
  [^org.symphonyoss.client.SymphonyClient session]
  (map #(hash-map :userId   (.getUid ^org.symphonyoss.symphony.pod.model.UserPresence %)
                  :presence (keyword (str (.getCategory ^org.symphonyoss.symphony.pod.model.UserPresence %))))
       (.getAllUserPresence (.getPresenceClient session))))
