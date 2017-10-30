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
  [^org.symphonyoss.symphony.clients.model.SymUser u]
  (if u
    {
      :user-id       (.getId           u)
      :username      (.getUsername     u)
      :email-address (.getEmailAddress u)
      :title         (.getTitle        u)
      :first-name    (.getFirstName    u)
      :last-name     (.getLastName     u)
      :display-name  (.getDisplayName  u)
      :company       (.getCompany      u)
      :location      (.getLocation     u)
      :avatars       (map #(hash-map :size (.getSize ^org.symphonyoss.symphony.clients.model.SymAvatar %)
                                     :url  (.getUrl  ^org.symphonyoss.symphony.clients.model.SymAvatar %))
                          (.getAvatars u))
    }))


(defmulti user-id
  "Returns the id (a Long) of the given user."
  {:arglists '([u])}
  (fn ([u] (type u))))

(defmethod user-id nil
  [u]
  nil)

(defmethod user-id Long
  [^Long u]
  u)

(defmethod user-id java.util.Map
  [{:keys [user-id]}]
  user-id)

(defmethod user-id org.symphonyoss.symphony.clients.model.SymUser
  [^org.symphonyoss.symphony.clients.model.SymUser u]
  (.getId u))


(defn userobjs
  "Returns all users in the pod, as org.symphonyoss.symphony.clients.model.SymUser objects.  Will throw an exception if the authenticated connection user doesn't have sufficient permissions."
  [^org.symphonyoss.client.SymphonyClient c]
  (.getAllUsers (.getUsersClient c)))


(defn users
  "Returns all users in the pod.  Will throw an exception if the authenticated connection user doesn't have sufficient permissions."
  [c]
  (map userobj->map (userobjs c)))


(defmulti userobj
  "Returns a org.symphonyoss.symphony.clients.model.SymUser object for the given user identifier, or the authenticated connection user if a user id is not provided. User can be specified either as a user id (Long) or an email address (String). Returns nil if the user doesn't exist.

Note: providing a user identifier requires calls to the server."
  {:arglists '([c]
               [c u])}
  (fn
    ([c]   :current-user)
    ([c u] (type u))))

(defmethod userobj :current-user
  [^org.symphonyoss.client.SymphonyClient c]
  (.getLocalUser c))

(defmethod userobj nil
  [c u]
  nil)

(defmethod userobj org.symphonyoss.symphony.clients.model.SymUser
  [c u]
  u)

(defmethod userobj Long
  [^org.symphonyoss.client.SymphonyClient c ^Long u]
  (try
    (.getUserFromId (.getUsersClient c) u)
    (catch org.symphonyoss.client.exceptions.UserNotFoundException unfe
      nil)))

(defmethod userobj String
  [^org.symphonyoss.client.SymphonyClient c ^String u]
  (try
    (.getUserFromEmail (.getUsersClient c) u)
    (catch org.symphonyoss.client.exceptions.UserNotFoundException unfe
      nil)))

(defmethod userobj java.util.Map
  [c {:keys [user-id]}]
  (userobj c user-id))


(defn user
  "Returns a user for the given user identifier, or the authenticated connection user if a user id is not provided."
  ([c]   (userobj->map (userobj c)))
  ([c u] (userobj->map (userobj c u))))


(defn userobj-by-username
  "Returns a org.symphonyoss.symphony.clients.model.SymUser object for the given username, or nil if the user doesn't exist."
  [^org.symphonyoss.client.SymphonyClient c ^String u]
  (.getUserFromName (.getUsersClient c) u))


(defn user-by-username
  "Returns a user for the given username, or nil if the user doesn't exist."
  [c u]
  (userobj->map (userobj-by-username c u)))


(defn- build-sym-user-obj
  [mode {:keys [username
                email-address
                title
                first-name
                last-name
                display-name
                company
                location
                avatars]}]
  (let [avatars-obj (if avatars
                       (map #(doto (org.symphonyoss.symphony.clients.model.SymAvatar.)
                               (.setSize (:size %))
                               (.setUrl  (:url  %)))
                            avatars))
        result       (doto (org.symphonyoss.symphony.clients.model.SymUser.)
                       (.setUsername     username)
                       (.setEmailAddress email-address)
                       (.setTitle        title)
                       (.setFirstName    first-name)
                       (.setLastName     last-name)
                       (.setDisplayName  display-name)
                       (.setCompany      company)
                       (.setLocation     location)
                       (.setAvatars      avatars-obj))]
    (if (= :create mode)   ; Set creation-only properties - these ones can't be changed post-creation
      (comment
      (doto result
;        (.setPublic   public)
;        (.setReadOnly read-only)
      )
      ))
    result))


(defn update-user!
  "Updates the details of an existing user, returning it as a map. The new user details are provided as a map with these keys:
  :user-id       The id of the user to update.
  :username      The new username of the user.  #### This might be create-only?
  :email-address The new email address of the user.
  :title         The new title of the user.
  :first-name    The new first name of the user.
  :last-name     The new last name of the user.
  :display-name  The new display name of the user.
  :company       The new company of the user.  #### This might be read-only?
  :location      The new location of the user.
  :avatars       A sequence of avatar maps, each of which contains these keys:
    :size        The 'size string' of the avatar (e.g. 'small', 'original')
    :url         The URL of the new avatar image file.
Note: calling this method requires that the service account calling this fn have the 'user administration' entitlement."
  [^org.symphonyoss.client.SymphonyClient c u]
  (if-let [user-id (:user-id u)]
    (userobj->map (.updateUser (.getUsersClient c) user-id (build-sym-user-obj :update u)))))


(defn same-pod?
  "Returns true if the given user is in the same pod as the authenticated connection user, or nil if the user doesn't exist."
  [c u]
  (let [me (user c)]
    (if-let [them (user c u)]
      (= (:company me) (:company them)))))


(defn cross-pod?
  "Returns true if the given user is in a different pod to the authenticated connection user, or nil if the user doesn't exist."
  [c u]
  (let [me (user c)]
    (if-let [them (user c u)]
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
  {:arglists '([c]
               [c u])}
  (fn
    ([c]   :current-user)
    ([c u] (type u))))

(defmethod presence :current-user
  [c]
  (presence c (user c)))

(defmethod presence nil
  [c u]
  nil)

(defmethod presence org.symphonyoss.symphony.clients.model.SymUser
  [c ^org.symphonyoss.symphony.clients.model.SymUser u]
  (presence c (.getId u)))

(defmethod presence Long
  [^org.symphonyoss.client.SymphonyClient c ^Long u]
  (keyword (str (.getCategory (.getUserPresence (.getPresenceClient c) u)))))

(defmethod presence String
  [c ^String u]
  (presence c (userobj c u)))

(defmethod presence java.util.Map
  [c {:keys [user-id]}]
  (if user-id
    (presence c user-id)))


(defmulti set-presence!
  "Sets the presence status of the given user, or the authenticated connection user if not provided.  The new presence must be one of presence-states."
  {:arglists '([c p]
               [c u p])}
  (fn
    ([c p]   :current-user)
    ([c u p] (type u))))

(defmethod set-presence! :current-user
  [c p]
  (set-presence! c (user c) p))

(defmethod set-presence! nil
  [c u p]
  nil)

(defmethod set-presence! org.symphonyoss.symphony.clients.model.SymUser
  [c ^org.symphonyoss.symphony.clients.model.SymUser u p]
  (set-presence! c (.getId u) p))

(defmethod set-presence! Long
  [^org.symphonyoss.client.SymphonyClient c ^Long u p]
  (let [presence (doto
                   (org.symphonyoss.symphony.pod.model.Presence.)
                   (.setCategory (org.symphonyoss.symphony.pod.model.Presence$CategoryEnum/valueOf (name p))))]
    (.setUserPresence (.getPresenceClient c) u presence)
    nil))

(defmethod set-presence! String
  [c ^String u p]
  (set-presence! c (userobj c u) p))

(defmethod set-presence! java.util.Map
  [c {:keys [user-id]} p]
  (set-presence! c user-id p))
