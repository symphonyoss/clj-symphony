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

(ns clj-symphony.chat
  "Operations related to 'chats'.  A 'chat' may contain 2 or more participants from 1 or 2 pods, and are different to rooms in that their membership is fixed at creation time."
  (:require [clj-symphony.user    :as syu]
            [clj-symphony.stream  :as sys]
            [clj-symphony.message :as sym]))


(defn chatobj->map
  "Converts a org.symphonyoss.client.model.Chat object into a map."
  [^org.symphonyoss.client.model.Chat ch]
  (if ch
    {
      :stream-id    (.getStreamId         ch)
      :last-message (sym/msgobj->map      (.getLastMessage ch))
      :other-users  (map syu/userobj->map (.getRemoteUsers ch))
    }))


(defn chatobjs
  "Returns all org.symphonyoss.client.model.Chat objects for the given user.  If no user identifier is provided, returns the chats of the authenticated connection user."
  ([c] (chatobjs c (syu/userobj c)))
  ([^org.symphonyoss.client.SymphonyClient c u]
    (.getChats (.getChatService c) (syu/userobj c u))))


(defn chats
  "Returns all chats for the given user.  If no user identifier is provided, returns the chats of the authenticated connection user."
  ([c]   (chats c (syu/user c)))
  ([c u] (map chatobj->map (chatobjs c u))))


(defmulti chatobj
  "Returns a Chat object for the given chat identifier (a org.symphonyoss.client.model.Chat, stream id, or map containing a :stream-id). Returns nil if the chat doesn't exist."
  {:arglists '([c ch])}
  (fn [c ch] (type ch)))

(defmethod chatobj nil
  [c ch]
  nil)

(defmethod chatobj org.symphonyoss.client.model.Chat
  [c ch]
  ch)

(defmethod chatobj String
  [^org.symphonyoss.client.SymphonyClient c ^String stream-id]
  (.getChatByStream (.getChatService c) stream-id))

(defmethod chatobj java.util.Map
  [c {:keys [stream-id]}]
  (if stream-id
    (chatobj c stream-id)))


(defn chat
  "Returns a chat as a map for the given chat identifier. Returns nil if the chat doesn't exist."
  [c ch]
  (chatobj->map (chatobj c ch)))


(defn start-chatobj!
  "Starts an :IM or :MIM chat with the specified user(s), returning the new org.symphonyoss.client.model.Chat object. Users can be provided either as a single user (as described in clj-symphony.user/userobj) or a sequence or set of such identifiers."
  [^org.symphonyoss.client.SymphonyClient c u]
  (let [user-objs (if (or (sequential? u) (set? u))
                    (set (map #(syu/userobj c %) u))
                    #{ (syu/userobj c u) })
        chat-obj  (doto
                    (org.symphonyoss.client.model.Chat.)
                    (.setLocalUser (syu/userobj c))
                    (.setRemoteUsers user-objs))
        _         (.addChat (.getChatService c) chat-obj)]
    chat-obj))


(defn start-chat!
  "Starts an :IM or :MIM chat with the specified user(s), returning the new chat as a map. Users can be provided either as a single user (as described in clj-symphony.user/userobj) or a sequence or set of such identifiers."
  [c u]
  (chatobj->map (start-chatobj! c u)))


(defn stop-chatobj!
  "Stops a chat.  Returns true if the chat was successfully stopped."
  [^org.symphonyoss.client.SymphonyClient c ^org.symphonyoss.client.model.Chat ch]
  (if ch
    (.removeChat (.getChatService c) ch)))


(defn stop-chat!
  "Stops a chat and returns true if the chat was successfully stopped, nil if the chat was invalid."
  [c ch]
  (if ch
    (stop-chatobj! c (chatobj c ch))))
