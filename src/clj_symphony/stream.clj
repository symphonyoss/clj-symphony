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

(ns clj-symphony.stream
  "Operations related to 'streams'.  A 'stream' is the generic term for any kind
  of message channel in the Symphony platform, and comes in one of several
  flavours:

  1. 1:1 chat
  2. M:M chat
  3. room
  4. wall post

  The primary difference between chats and rooms is that rooms have dynamic
  membership whereas the membership of a chat is fixed at creation time.

  In addition, each type of stream can be 'internal' (intra-pod) or 'external'
  (include users from at most 1 other pod, aka 'cross-pod')."
  (:require [clj-symphony.user :as syu]))


(def stream-types
  "The set of possible stream types in Symphony, as keywords."
  (set (map #(keyword (str %)) (org.symphonyoss.symphony.clients.model.SymStreamTypes$Type/values))))


(defn streamobj->map
  "Converts a `org.symphonyoss.symphony.clients.model.SymStreamAttributes` object
  into a map with these keys:

  | Key                | Description                                                  |
  |--------------------|--------------------------------------------------------------|
  | `:stream-id`       | The stream id of the room.                                   |
  | `:name`            | The name of the stream (if any).                             |
  | `:active`          | A boolean indicating whether the stream is active or not.    |
  | `:type`            | The type of the stream (see [[stream-types]]).               |
  | `:cross-pod`       | A boolean indicating whether the stream is cross-pod or not. |
  | `:member-user-ids` | A sequence of the user ids of members of the stream.         |
  "
  [^org.symphonyoss.symphony.clients.model.SymStreamAttributes s]
  (if s
    {
      :stream-id          (.getId               s)
      :name               (if-let [room-attrs (.getSymRoomSpecificStreamAttributes s)]
                            (.getName room-attrs))
      :active             (.getActive           s)
      :type               (when-not (nil? (.getSymStreamTypes s))
                            (keyword (str (.getType (.getSymStreamTypes s)))))
      :cross-pod          (.getCrossPod         s)
      :member-user-ids    (if-let [chat-attrs (.getSymChatSpecificStreamAttributes s)]
                            (vec (.getMembers chat-attrs)))
    }))


(defmulti stream-id
  "Returns the stream id of the given stream."
  {:arglists '([s])}
  type)

(defmethod stream-id nil
  [s]
  nil)

(defmethod stream-id String
  [^String s]
  s)

(defmethod stream-id java.util.Map
  [{:keys [stream-id]}]
  stream-id)

(defmethod stream-id org.symphonyoss.symphony.clients.model.SymStreamAttributes
  [^org.symphonyoss.symphony.clients.model.SymStreamAttributes s]
  (.getId s))

(defmethod stream-id org.symphonyoss.client.model.Chat
  [^org.symphonyoss.client.model.Chat s]
  (.getStreamId s))

(defmethod stream-id org.symphonyoss.symphony.clients.model.SymRoomDetail
  [^org.symphonyoss.symphony.clients.model.SymRoomDetail s]
  (.getId (.getRoomSystemInfo s)))

(defmethod stream-id org.symphonyoss.symphony.clients.model.SymMessage
  [^org.symphonyoss.symphony.clients.model.SymMessage s]
  (.getStreamId s))


(defn streamobjs
  "Returns a list of `org.symphonyoss.symphony.clients.model.SymStreamAttributes`
  objects visible to the authenticated connection user."
  [^org.symphonyoss.client.SymphonyClient c]
  (.getStreams (.getStreamsClient c)
               nil
               nil
               (org.symphonyoss.symphony.clients.model.SymStreamFilter.)))


(defn streams
  "Returns a lazy sequence of streams (as maps, see [[streamobj->map]] for
  details) visible to the authenticated connection user."
  [c]
  (map streamobj->map (streamobjs c)))


(defn streamobj
  "Returns the given stream identifier as a
  `org.symphonyoss.symphony.clients.model.SymStreamAttributes` object, or `nil`
  if it doesn't exist / isn't accessible to the authenticated connection user."
  [^org.symphonyoss.client.SymphonyClient c s]
  (.getStreamAttributes (.getStreamsClient c) (stream-id s)))


(defn stream
  "Returns the given stream identifier as a map (see [[streamobj->map]] for
  details), or `nil` if it doesn't exist / isn't accessible to the authenticated
  connection user."
  [c s]
  (streamobj->map (streamobj c s)))


(defn- stream-type-fn
  [c s]
  (:type (stream c s)))
(def
  ^{:arglists '([c s])}
  stream-type
  "Returns the type of the given stream identifier (see [[stream-types]] for the
  full set of possible values). Results are cached (via `memoize`)."
  (memoize stream-type-fn))


(defn usersobjs-from-stream
  "Returns all `org.symphonyoss.symphony.clients.model.SymUser` objects
  participating in the given stream."
  [^org.symphonyoss.client.SymphonyClient c s]
  (.getUsersFromStream (.getUsersClient c) (stream-id s)))


(defn users-from-stream
  "Returns all users participating in the given stream, as a sequence of maps (see
  [[clj-symphony.user/userobj->map]] for details)."
  [c s]
  (map syu/userobj->map (usersobjs-from-stream c s)))
