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
  "Operations related to 'streams'.  A 'stream' is the generic term for any kind of message channel in the Symphony platform, and come in several flavours:
1. 1:1 chat
2. M:M chat
3. room

The primary difference between chats and rooms is that rooms have dynamic membership whereas the membership of a chat is fixed at creation time.

In addition, each type of stream can be 'internal' (intra-pod) or 'external' (include users from 1 other pod)."
  (:require [clj-symphony.user :as syu]))


(defn streamobj->map
  "Converts a SymAdminStreamInfo object into a map."
  [^org.symphonyoss.symphony.clients.model.SymAdminStreamInfo stream]
  (if stream
    {
      :stream-id          (.getId               stream)
      :created-by-user-id (.getCreatedByUserId  (.getAttributes stream))
      :created-date       (.getCreatedDate      (.getAttributes stream))
      :modified-date      (.getLastModifiedDate (.getAttributes stream))
      :name               (.getRoomName         (.getAttributes stream))
      :origin-company     (.getOriginCompany    (.getAttributes stream))
      :origin-company-id  (.getOriginCompanyId  (.getAttributes stream))
      :external           (.getIsExternal       stream)
      :public             (.getIsPublic         stream)
      :active             (.getIsActive         stream)
      :type               (keyword (.getType    stream))
      :members-count      (.getMembersCount     (.getAttributes stream))
      :member-user-ids    (.getMembers          (.getAttributes stream))
    }))


(defn get-streamobjs
  "Returns all SymAdminStreamInfo objects visible to the authenticated connection user."
  [^org.symphonyoss.client.SymphonyClient connection]
  (.getStreams (.getStreams (.getStreamsClient connection)
                            nil
                            nil
                            (org.symphonyoss.symphony.clients.model.SymAdminStreamFilter.))))

(defn get-streams
  "Returns all streams visible to the authentication connection user."
  [connection]
  (map streamobj->map (get-streamobjs connection)))


; Note: currently SJC doesn't seem to offer any way to get stream information
; for a single stream, so we emulate it here until such time as it does
(defn get-streamobj
  "Returns the given stream as a SymAdminStreamInfo object, or nil if it doesn't exist / isn't accessible to the authenticated connection user.
WARNING: this method is expensive and inefficient!  Use it with caution!"
  [connection ^String stream-id]
  (first (filter #(= stream-id (.getId ^org.symphonyoss.symphony.clients.model.SymAdminStreamInfo %))
                 (get-streamobjs connection))))


(defn get-stream
  "Returns the given stream as a map, or nil if it doesn't exist / isn't accessible to the authenticated connection user.
WARNING: this method is expensive and inefficient!  Use it with caution!"
  [connection stream-id]
  (streamobj->map (get-streamobj connection stream-id)))


(defmulti get-userobjs-from-stream
  "Returns all SymUser objects participating in the given stream."
  (fn [connection stream-id] (type stream-id)))

(defmethod get-userobjs-from-stream String
  [^org.symphonyoss.client.SymphonyClient connection ^String stream-id]
  (.getUsersFromStream (.getUsersClient connection) stream-id))

(defmethod get-userobjs-from-stream java.util.Map
  [connection {:keys [stream-id]}]
  (if stream-id
    (get-userobjs-from-stream connection stream-id)))


(defn get-users-from-stream
  "Returns all users participating in the given stream."
  [connection stream]
  (map syu/userobj->map (get-userobjs-from-stream connection stream)))

