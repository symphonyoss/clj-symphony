;
; Copyright 2017 Fintech Open Source Foundation
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
  "Operations related to 'rooms'.  A 'room' is a named chat containing 1 or more
  participants from 1 or 2 pods."
  (:require [clj-symphony.user    :as syu]
            [clj-symphony.stream  :as sys]
            [clj-symphony.message :as sym]))


(defn roomobj->map
  "Converts a `org.symphonyoss.symphony.clients.model.SymRoomDetail` object into
  a map with these keys:

  | Key                   | Description                                                                          |
  |-----------------------|--------------------------------------------------------------------------------------|
  | `:stream-id`          | The stream id of the room.                                                           |
  | `:creation-date`      | The creation date of the room.                                                       |
  | `:created-by-user-id` | The user id of the user who created the room.                                        |
  | `:active`             | A boolean indicating whether the room is active or not.                              |
  | `:name`               | The name of the room.                                                                |
  | `:description`        | The description of the room.                                                         |
  | `:public`             | A boolean indicating whether the room is public.                                     |
  | `:read-only`          | A boolean indicating whether the room is read only.                                  |
  | `:discoverable`       | A boolean indicating whether the room is discoverable (can be found via search).     |
  | `:copy-protected`     | A boolean indicating whether the room is copy protected.                             |
  | `:can-members-invite` | A boolean indicating whether members of the room can invite other users to the room. |
  | `:keywords`           | A map of 'keywords' (key/value pairs, both of which are strings) for the room.       |
"
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
  "Returns an `org.symphonyoss.symphony.clients.model.SymRoomDetail` object for
  the given room  (as a stream id or map containing a `:stream-id`). Returns
  `nil` if the room doesn't exist."
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
  "Returns the given room as a map (see [[roomobj->map]] for details), or `nil`
  if the room doesn't exist. `r` can be anything supported by [[roomobj]]."
  [c r]
  (roomobj->map (roomobj c r)))


(defn roomobjs
  "Returns a lazy sequence containing all `org.symphonyoss.symphony.clients.model.SymRoomDetail`
  objects for the authenticated connection user.

  **WARNING:** this methods results in many calls to the server.  Use with caution!"
  [c]
  (let [rooms (filter #(= :ROOM (:type %)) (sys/streams c))]
    (map (partial roomobj c) rooms)))


(defn rooms
  "Returns a lazy sequence containing all rooms (as maps) for the authenticated
  connection user.

  **WARNING:** this methods results in many calls to the server.  Use with caution!"
  [c]
  (map roomobj->map (roomobjs c)))


(defn room-members
  "Returns all users participating in the given room, as a sequence of maps (see
  [[clj-symphony.user/userobj->map]] for details on the map structure)."
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
  "Creates a new room and returns the `org.symphonyoss.symphony.clients.model.SymRoomDetail`
  object for it. The details of the room can be provided as a
  `org.symphonyoss.symphony.clients.model.SymRoomAttributes` object, or a map
  with these keys:

  | Key                   | Description                                                                                |
  |-----------------------|--------------------------------------------------------------------------------------------|
  | `:name`               | Name of the room.                                                                          |
  | `:description`        | Description of the room.                                                                   |
  | `:public`             | Boolean indicating whether the room is 'public' (i.e. cross-pod enabled).                  |
  | `:read-only`          | Boolean indicating whether the room is read/only.                                          |
  | `:discoverable`       | Boolean indicating whether the room is discoverable (searchable).                          |
  | `:copy-protected`     | Boolean indicating whether the room is copy protected.                                     |
  | `:can-members-invite` | Boolean indicating whether members can invite others to the room.                          |
  | `:keywords`           | A map containing 'keywords' (key/value pairs, both of which must be strings) for the room. |

  `:name` is mandatory (it must be present and cannot be `nil`)."
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
  "Create a new room, returning it as a map.  See [[create-roomobj!]] for
  details."
  [c r]
  (roomobj->map (create-roomobj! c r)))


(defn update-room!
  "Updates the details of an existing room (provided as a map), returning the
  updated room (as a map).

  The new room details map may contain these keys (additional keys offered by
  [[create-roomobj!]] cannot be modified post-creation):

  | Key                   | Description                                                                                        |
  |-----------------------|----------------------------------------------------------------------------------------------------|
  | `:stream-id`          | The stream id of the room.                                                                         |
  | `:name`               | The new name for the room.                                                                         |
  | `:description`        | The new description of the room.                                                                   |
  | `:discoverable`       | Boolean indicating whether the room is to become discoverable (searchable).                        |
  | `:copy-protected`     | Boolean indicating whether the room is to become copy protected.                                   |
  | `:can-members-invite` | Boolean indicating whether members will be allowed to invite others to the room.                   |
  | `:keywords`           | A map containing the new 'keywords' (key/value pairs, both of which must be strings) for the room. |

  `:stream-id` is mandatory (it must be present and cannot be `nil`)."
  [^org.symphonyoss.client.SymphonyClient c r]
  (if-let [stream-id (:stream-id r)]
    (roomobj->map (.updateChatRoom (.getStreamsClient c) stream-id (build-sym-room-attributes-obj :update r)))))


(defn deactivate-room!
  "Deactivates the given room."
  [^org.symphonyoss.client.SymphonyClient c r]
  (.deactivateRoom (.getStreamsClient c) (sys/stream-id r))
  nil)


(defn add-user-to-room!
  "Add a user (either identified as per [[clj-symphony.user/user-id]], or using
  an email address) to the given room."
  [^org.symphonyoss.client.SymphonyClient c r u]
  (let [stream-id (sys/stream-id r)
        user-id   (if (instance? String u)         ; ####TODO: move this logic to user ns?
                    (syu/user-id (syu/user c u))
                    (syu/user-id u))]
    (.addMemberToRoom (.getRoomMembershipClient c) stream-id user-id)
    nil))


(defn add-users-to-room!
  "Add all of the provided users (identified as per [[clj-symphony.user/user-id]],
  or using an email address) to the given room."
  [c r u]
  (doall (map (partial add-user-to-room! c r) u))
  nil)


(defn remove-user-from-room!
  "Remove a user (either identified as per [[clj-symphony.user/user-id]], or
  using an email address) from the given room."
  [^org.symphonyoss.client.SymphonyClient c r u]
  (let [stream-id (sys/stream-id r)
        user-id   (if (instance? String u)
                    (syu/user-id (syu/user c u))
                    (syu/user-id u))]
    (.removeMemberFromRoom (.getRoomMembershipClient c) stream-id user-id)
    nil))


(defn remove-users-from-room!
  "Remove all of the provided users (identified as per [[clj-symphony.user/user-id]],
  or using an email address) from the given room."
  [c r u]
  (doall (map (partial remove-user-from-room! c r) u))
  nil)
