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

(ns clj-symphony.room
  "Operations related to 'rooms'.  A 'room' is a named chat containing 1 or more participants from 1 or 2 pods."
  (:require [clj-symphony.user    :as syu]
            [clj-symphony.stream  :as sys]
            [clj-symphony.message :as sym]))


(defn roomobj->map
  "Converts a org.symphonyoss.symphony.clients.model.SymRoomDetail object into a map."
  [^org.symphonyoss.symphony.clients.model.SymRoomDetail r]
  (if r
    (let [sys-info (.getRoomSystemInfo r)
          attrs    (.getRoomAttributes r)]
      {
        :stream-id          (.getId              sys-info)
        :creation-date      (.getCreationDate    sys-info)
        :created-by-user-id (.getCreatedByUserId sys-info)
        :active             (.getActive          sys-info)
        :name               (.getName             attrs)
        :description        (.getDescription      attrs)
        :public             (.getPublic           attrs)
        :read-only          (.getReadOnly         attrs)
        :discoverable       (.getDiscoverable     attrs)
        :copy-protected     (.getCopyProtected    attrs)
        :can-members-invite (.getMembersCanInvite attrs)
        :keywords           (into {} (map #(hash-map (keyword (.getKey ^org.symphonyoss.symphony.clients.model.SymRoomTag %))
                                                     (.getValue ^org.symphonyoss.symphony.clients.model.SymRoomTag %))
                                          (.getKeywords attrs)))
      })))


(defmulti roomobj
  "Returns a org.symphonyoss.symphony.clients.model.SymRoomDetail object for the given room  (as a stream id or map containing a :stream-id). Returns nil if the room doesn't exist."
  {:arglists '([c r])}
  (fn [c r] (type r)))

(defmethod roomobj nil
  [c r]
  nil)

(defmethod roomobj org.symphonyoss.symphony.clients.model.SymRoomDetail
  [c r]
  r)

(defmethod roomobj String
  [^org.symphonyoss.client.SymphonyClient c ^String s]
  (.getRoomDetail (.getStreamsClient c) s))

(defmethod roomobj java.util.Map
  [c {:keys [stream-id]}]
  (if stream-id
    (roomobj c stream-id)))


(defn room
  "Returns a room as a map for the given room. Returns nil if the room doesn't exist."
  [c r]
  (roomobj->map (roomobj c r)))


(defn roomobjs
  "Returns a lazy sequence containing all org.symphonyoss.symphony.clients.model.SymRoomDetail objects for the authenticated connection user.

WARNING: this methods results in many calls to the server.  Use with caution!"
  [c]
  (let [rooms (filter #(= :ROOM (:type %)) (sys/streams c))]
    (map (partial roomobj c) rooms)))


(defn rooms
  "Returns a lazy sequence containing all rooms (as maps) for the authenticated connection user.

WARNING: this methods results in many calls to the server.  Use with caution!"
  [c]
  (map roomobj->map (roomobjs c)))


(defn room-members
  "Returns the users who are participants in the given roon."
  [c r]
  (sys/users-from-stream c r))
;  (.getRoomMembership (.getRoomMembershipClient connection) (sys/stream-id room))


(defn- build-sym-room-attributes-obj
  [mode {:keys [name
                description
                public
                read-only
                discoverable
                copy-protected
                can-members-invite
                keywords]}]
  (let [keywords-obj (if keywords
                       (map #(doto (org.symphonyoss.symphony.clients.model.SymRoomTag.)
                               (.setKey   (clojure.core/name (first %)))
                               (.setValue (str (second %))))
                            keywords))
        result       (doto (org.symphonyoss.symphony.clients.model.SymRoomAttributes.)
                       (.setName             name)
                       (.setDescription      description)
                       (.setKeywords         keywords-obj)
                       (.setDiscoverable     discoverable)
                       (.setCopyProtected    copy-protected)
                       (.setMembersCanInvite can-members-invite))]
    (if (= :create mode)   ; Set creation-only properties - these ones can't be changed post-creation
      (doto result
        (.setPublic   public)
        (.setReadOnly read-only)))
    result))


(defmulti create-roomobj!
  "Creates a new room and returns the org.symphonyoss.symphony.clients.model.SymRoomDetail object for it. The details of the room can be provided as a org.symphonyoss.symphony.clients.model.SymRoomAttributes object, or a map with these keys:
  :name               Name of the room.
  :description        Description of the room.
  :public             Boolean indicating whether the room is 'public' (i.e. cross-pod enabled).
  :read-only          Boolean indicating whether the room is read/only.
  :discoverable       Boolean indicating whether the room is discoverable (searchable).
  :copy-protected     Boolean indicating whether the room is copy protected.
  :can-members-invite Boolean indicating whether members can invite others to the room.
  :keywords           A map containing 'keywords' (key/value pairs, both of which must be strings) for the room.

:name is mandatory (it must be present and cannot be nil)."
  {:arglists '([c r])}
  (fn [c r] (type r)))

(defmethod create-roomobj! org.symphonyoss.symphony.clients.model.SymRoomAttributes
  [^org.symphonyoss.client.SymphonyClient                    c
   ^org.symphonyoss.symphony.clients.model.SymRoomAttributes r]
  (.createChatRoom (.getStreamsClient c) r))

(defmethod create-roomobj! java.util.Map
  [c r]
  (create-roomobj! c (build-sym-room-attributes-obj :create r)))


(defn create-room!
  "Create a new room, returning it as a map.  See clj-symphony.room/create-roomobj! for details."
  [c r]
  (roomobj->map (create-roomobj! c r)))


(defn update-room!
  "Updates the details of an existing room, returning it as a map. The map these keys (the additional flags available during creation cannot be modified):
  :stream-id          The stream id of the room.
  :name               The new name for the room.
  :description        The new description of the room.
  :discoverable       Boolean indicating whether the room is to become discoverable (searchable).
  :copy-protected     Boolean indicating whether the room is to become copy protected.
  :can-members-invite Boolean indicating whether members will be allowed to invite others to the room.
  :keywords           A map containing the new 'keywords' (key/value pairs, both of which must be strings) for the room."
  [^org.symphonyoss.client.SymphonyClient c r]
  (if-let [stream-id (:stream-id r)]
    (roomobj->map (.updateChatRoom (.getStreamsClient c) stream-id (build-sym-room-attributes-obj :update r)))))


(defn deactivate-room!
  "Deactivates the given room."
  [^org.symphonyoss.client.SymphonyClient c r]
  (.deactivateRoom (.getStreamsClient c) (sys/stream-id r))
  nil)


(defn add-user-to-room!
  "Add a user to the given room."
  [^org.symphonyoss.client.SymphonyClient c r u]
  (let [stream-id (sys/stream-id r)
        user-id   (if (instance? String u)
                    (syu/user-id (syu/user c u))
                    (syu/user-id u))]
    (.addMemberToRoom (.getRoomMembershipClient c) stream-id user-id)
    nil))


(defn add-users-to-room!
  "Add all of the provided users to the given room."
  [c r u]
  (doall (map (partial add-user-to-room! c r) u))
  nil)


(defn remove-user-from-room!
  "Remove a user from the given room."
  [^org.symphonyoss.client.SymphonyClient c r u]
  (let [stream-id (sys/stream-id r)
        user-id   (if (instance? String u)
                    (syu/user-id (syu/user c u))
                    (syu/user-id u))]
    (.removeMemberFromRoom (.getRoomMembershipClient c) stream-id user-id)
    nil))


(defn remove-users-from-room!
  "Remove all of the provided users from the given room."
  [c r u]
  (doall (map (partial remove-user-from-room! c r) u))
  nil)
