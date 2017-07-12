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
        :keywords           (into {} (map #(hash-map (.getKey ^org.symphonyoss.symphony.clients.model.SymRoomTag %) (.getValue ^org.symphonyoss.symphony.clients.model.SymRoomTag %)) (.getKeywords attrs)))
      })))


(defmulti get-roomobj
  "Returns a SymRoomDetail object for the given room identifier (as a stream id or map containing a :stream-id).
Returns nil if the room doesn't exist."
  {:arglists '([connection room-identifier])}
  (fn [connection room-identifier] (type room-identifier)))

(defmethod get-roomobj nil
  [connection chat-id]
  nil)

(defmethod get-roomobj org.symphonyoss.symphony.clients.model.SymRoomDetail
  [connection room]
  room)

(defmethod get-roomobj String
  [^org.symphonyoss.client.SymphonyClient connection ^String stream-id]
  (.getRoomDetail (.getStreamsClient connection) stream-id))

(defmethod get-roomobj java.util.Map
  [connection {:keys [stream-id]}]
  (if stream-id
    (get-roomobj connection stream-id)))


(defn get-room
  "Returns a room as a map for the given room identifier.
Returns nil if the room doesn't exist."
  [connection room-identifier]
  (roomobj->map (get-roomobj connection room-identifier)))


(defn get-roomobjs
  "Returns all SymRoomDetail objects for the authenticated connection user.

WARNING: this methods results in many calls to the server.  Use with caution!"
  [^org.symphonyoss.client.SymphonyClient connection]
  (let [rooms (filter #(= :ROOM (:type %)) (sys/get-streams connection))]
    (map (partial get-roomobj connection) rooms)))


(defn get-rooms
  "Returns all rooms (as maps) for the authenticated connection user.

WARNING: this methods results in many calls to the server.  Use with caution!"
  [connection] (map roomobj->map (get-roomobjs connection)))

