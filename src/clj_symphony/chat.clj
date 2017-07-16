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
  "Converts a Chat object into a map."
  [^org.symphonyoss.client.model.Chat chat]
  (if chat
    {
      :stream-id    (.getStreamId         chat)
      :last-message (sym/msgobj->map      (.getLastMessage chat))
      :other-users  (map syu/userobj->map (.getRemoteUsers chat))
    }))


(defn chatobjs
  "Returns all Chat objects for the given user.  If no user identifier is provided, returns the chats of the authenticated connection user."
  ([connection] (chatobjs connection (syu/userobj connection)))
  ([^org.symphonyoss.client.SymphonyClient connection user-id]
    (let [user (syu/userobj connection user-id)]
      (.getChats (.getChatService connection) user))))


(defn chats
  "Returns all chats for the given user.  If no user identifier is provided, returns the chats of the authenticated connection user."
  ([connection]         (chats connection (syu/user connection)))
  ([connection user-id] (map chatobj->map (chatobjs connection user-id))))


(defmulti chatobj
  "Returns a Chat object for the given chat identifier (as a stream id or map containing a :stream-id). Returns nil if the chat doesn't exist."
  {:arglists '([connection chat-identifier])}
  (fn [connection chat-identifier] (type chat-identifier)))

(defmethod chatobj nil
  [connection chat-id]
  nil)

(defmethod chatobj org.symphonyoss.client.model.Chat
  [connection chat]
  chat)

(defmethod chatobj String
  [^org.symphonyoss.client.SymphonyClient connection ^String stream-id]
  (.getChatByStream (.getChatService connection) stream-id))

(defmethod chatobj java.util.Map
  [connection {:keys [stream-id]}]
  (if stream-id
    (chatobj connection stream-id)))


(defn chat
  "Returns a chat as a map for the given chat identifier. Returns nil if the chat doesn't exist."
  [connection chat-identifier]
  (chatobj->map (chatobj connection chat-identifier)))


(defn start-chatobj!
  "Starts an :IM or :MIM chat with the specified user(s), returning the new chat object. 'users' can be either a single user-identifier (as described in clj-symphony.user/userobj) or a sequence or set of such identifiers."
  [^org.symphonyoss.client.SymphonyClient connection users]
  (let [user-objs    (map (partial syu/userobj connection) users)
        remote-users (if (or (sequential? users) (set? users))
                       (set (map #(syu/userobj connection %) users))
                       #{(syu/userobj connection users)})
        chat-obj     (doto
                       (org.symphonyoss.client.model.Chat.)
                       (.setLocalUser (syu/userobj connection))
                       (.setRemoteUsers remote-users))
        _            (.addChat (.getChatService connection) chat-obj)]
    chat-obj))


(defn start-chat!
  "Starts an :IM or :MIM chat with the specified user(s), returning the new chat as a map. 'users' can be either a single user-identifier (as described in clj-symphony.user/userobj) or a sequence or set of such identifiers."
  [connection users]
  (chatobj->map (start-chatobj! connection users)))


(defn stop-chatobj!
  "Stops a chat.  Returns true if the chat was successfully stopped."
  [^org.symphonyoss.client.SymphonyClient connection ^org.symphonyoss.client.model.Chat chat]
  (if chat
    (.removeChat (.getChatService connection) chat)))


(defn stop-chat!
  "Stops a chat, identified by the given chat identifier (as described in get-chatobj).  Returns true if the chat was successfully stopped, nil if the chat identifier was invalid."
  [connection chat-identifier]
  (stop-chatobj! connection (chatobj connection chat-identifier)))
