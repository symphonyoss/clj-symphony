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
  "Converts a Room object into a map."
  [^org.symphonyoss.symphony.clients.model.SymRoomDetail room]
  (if room
    (let [sys-info (.getRoomSystemInfo room)
          attrs    (.getRoomAttributes room)]
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
  "Returns a SymRoomDetail object for the given room identifier (as a stream id or map containing a :stream-id). Returns nil if the room doesn't exist."
  {:arglists '([connection room-identifier])}
  (fn [connection room-identifier] (type room-identifier)))

(defmethod roomobj nil
  [connection room-id]
  nil)

(defmethod roomobj org.symphonyoss.symphony.clients.model.SymRoomDetail
  [connection room]
  room)

(defmethod roomobj String
  [^org.symphonyoss.client.SymphonyClient connection ^String stream-id]
  (.getRoomDetail (.getStreamsClient connection) stream-id))

(defmethod roomobj java.util.Map
  [connection {:keys [stream-id]}]
  (if stream-id
    (roomobj connection stream-id)))


(defn room
  "Returns a room as a map for the given room identifier. Returns nil if the room doesn't exist."
  [connection room-identifier]
  (roomobj->map (roomobj connection room-identifier)))


(defn roomobjs
  "Returns a lazy sequence containing all SymRoomDetail objects for the authenticated connection user.

WARNING: this methods results in many calls to the server.  Use with caution!"
  [connection]
  (let [rooms (filter #(= :ROOM (:type %)) (sys/streams connection))]
    (map (partial roomobj connection) rooms)))


(defn rooms
  "Returns a lazy sequence containing all rooms (as maps) for the authenticated connection user.

WARNING: this methods results in many calls to the server.  Use with caution!"
  [connection]
  (map roomobj->map (roomobjs connection)))


(defn room-members
  "Returns the users who are participants in the given roon."
  [connection room]
  (sys/users-from-stream connection room))
;  (.getRoomMembership (.getRoomMembershipClient connection) (sys/stream-id room))


(defmulti create-room-obj!
  "Creates a new room and returns the SymRoomDetail object for it. The details of the room can be provided as a org.symphonyoss.symphony.clients.model.SymRoomAttributes object, or a map with these keys:
  :name               Name of the room.
  :description        Description of the room.
  :public             Boolean indicating whether the room is 'public' (i.e. cross-pod enabled).
  :read-only          Boolean indicating whether the room is read/only.
  :discoverable       Boolean indicating whether the room is discoverable (searchable).
  :copy-protected     Boolean indicating whether the room is copy protected.
  :can-members-invite Boolean indicating whether members can invite others to the room.
  :keywords           A map containing 'keywords' (key/value pairs) for the room.

:name is mandatory (it must be present and cannot be nil)."
  {:arglists '([connection room-details])}
  (fn [connection room-details] (type room-details)))

(defmethod create-room-obj! org.symphonyoss.symphony.clients.model.SymRoomAttributes
  [^org.symphonyoss.client.SymphonyClient                    connection
   ^org.symphonyoss.symphony.clients.model.SymRoomAttributes room-details]
  (.createChatRoom (.getStreamsClient connection) room-details))

(defmethod create-room-obj! java.util.Map
  [connection {:keys [name
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
        room-details (doto (org.symphonyoss.symphony.clients.model.SymRoomAttributes.)
                       (.setName             name)
                       (.setDescription      description)
                       (.setPublic           public)
                       (.setReadOnly         read-only)
                       (.setDiscoverable     discoverable)
                       (.setCopyProtected    copy-protected)
                       (.setMembersCanInvite can-members-invite)
                       (.setKeywords         keywords-obj))]
    (create-room-obj! connection room-details)))


(defn create-room!
  "Create a new room, returning it as a map.  See clj-symphony.room/create-room-obj! for details on 'room-details'."
  [connection room-details]
  (roomobj->map (create-room-obj! connection room-details)))


(defn deactivate-room!
  "Deactivates the given room."
  [^org.symphonyoss.client.SymphonyClient connection room]
  (.deactivateRoom (.getStreamsClient connection) (sys/stream-id room))
  nil)


(defn add-user-to-room!
  "Add a user to the given room."
  [^org.symphonyoss.client.SymphonyClient connection room user]
  (let [stream-id (sys/stream-id room)
        user-id   (if (instance? String user)
                    (syu/user-id (syu/user connection user))
                    (syu/user-id user))]
    (.addMemberToRoom (.getRoomMembershipClient connection) stream-id user-id)
    nil))


(defn add-users-to-room!
  "Add all of the provided users to the given room."
  [connection room users]
  (doall (map (partial add-user-to-room! connection room) users))
  nil)


(defn remove-user-from-room!
  "Remove a user from the given room."
  [^org.symphonyoss.client.SymphonyClient connection room user]
  (let [stream-id (sys/stream-id room)
        user-id   (if (instance? String user)
                    (syu/user-id (syu/user connection user))
                    (syu/user-id user))]
    (.removeMemberFromRoom (.getRoomMembershipClient connection) stream-id user-id)
    nil))


(defn remove-users-from-room!
  "Remove all of the provided users from the given room."
  [connection room users]
  (doall (map (partial remove-user-from-room! connection room) users))
  nil)
