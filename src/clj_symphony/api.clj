;
; Copyright © 2016, 2017 Symphony Software Foundation
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

(ns clj-symphony.api
  (:require [clojure.string :as s]))

(defn- mapify
  [x]
  (dissoc (bean x) :class))

;(defn get-chats
;  "Returns a list of chats for the given user, or for the authenticated connection user if a user id is not provided."
;  ([^org.symphonyoss.client.SymphonyClient connection]               (map mapify (.getChats (.getChatService connection) (user connection))))
;  ([^org.symphonyoss.client.SymphonyClient connection ^Long user-id] (map mapify (.getChats (.getChatService connection) (user connection user-id)))))

;(defn establish-chat
;  "Establishes a chat with the given user."
;  [^org.symphonyoss.client.SymphonyClient connection user-identifier]
;  (let [recipient #{(user connection user-identifier)}
;        chat      (org.symphonyoss.client.model.Chat.)
;        _         (.setLocalUser   chat (user connection))
;        _         (.setRemoteUsers chat recipient)
;        _         (.setStream      chat (.getStream (.getStreamsClient connection) ^java.util.Set recipient))]
;    chat))

(defmulti send-message!
  "Sends a message to the given chat, room or stream.  Both text and MessageML messages are supported."
  (fn [connection target message] (type target)))

(defn- build-sym-message
  [^String message]
  (let [msg (org.symphonyoss.symphony.clients.model.SymMessage.)
        _  (.setMessage msg message)]
    (if (.startsWith message "<messageML>")
      (.setFormat msg org.symphonyoss.symphony.clients.model.SymMessage$Format/MESSAGEML)
      (.setFormat msg org.symphonyoss.symphony.clients.model.SymMessage$Format/TEXT))
    msg))

(defmethod send-message! org.symphonyoss.client.model.Chat
  [^org.symphonyoss.client.SymphonyClient connection ^org.symphonyoss.client.model.Chat chat ^String message]
  (.sendMessage (.getMessageService connection)
                chat
                ^org.symphonyoss.symphony.clients.model.SymMessage (build-sym-message message))
  nil)

(defmethod send-message! org.symphonyoss.client.model.Room
  [^org.symphonyoss.client.SymphonyClient connection ^org.symphonyoss.client.model.Room room ^String message]
  (.sendMessage (.getMessageService connection)
                room
                ^org.symphonyoss.symphony.clients.model.SymMessage (build-sym-message message))
  nil)

(defmethod send-message! String
  [^org.symphonyoss.client.SymphonyClient connection ^String stream-id ^String message]
  (let [stream (org.symphonyoss.symphony.pod.model.Stream.)
        _      (.setId stream stream-id)]
    (.sendMessage (.getMessagesClient connection)
                  stream
                  ^org.symphonyoss.symphony.clients.model.SymMessage (build-sym-message message))
    nil))

(defn register-message-listener
  "Registers f, a function of 7 parameters, as a message listener (callback), and returns a handle to that listener
  so that it can be deregistered later on, if needed.  The 7 arguments passed to f are:

     msg-id     - Identifier of the message
     timestamp  - Timestamp the message was sent (as a string in ####???? format)
     stream-id  - Identifier of the stream (chat or room) the message was sent to)
     user-id    - Identifier of the user who sent the message
     msg-format - Format of the message as a keyword (:messageml or :text)
     msg-type   - ####????
     msg        - Text of the message

   The value returned by f (if any) is ignored."
  [^org.symphonyoss.client.SymphonyClient connection f]
  (let [listener (reify
                   org.symphonyoss.client.services.MessageListener
                   (onMessage [this msg]
                     (let [msg-t      ^org.symphonyoss.symphony.clients.model.SymMessage msg
                           msg-id     (.getId          msg-t)
                           timestamp  (.getTimestamp   msg-t)
                           stream-id  (.getStreamId    msg-t)
                           user-id    (.getFromUserId  msg-t)
                           msg-format (when-not (nil? (.getFormat msg-t))
                                        (keyword (s/lower-case (str (.getFormat msg-t)))))
                           msg-type   (.getMessageType msg-t)
                           msg-text   (.getMessage     msg-t)]
                      (f msg-id timestamp stream-id user-id msg-format msg-type msg-text))))]
    (.addMessageListener (.getMessageService connection) listener)
    listener))

(defn deregister-message-listener
  "Deregisters a previously-registered message listener.  Once deregistered, a listener should be discarded.
  Returns true if a valid message listener was deregistered, false otherwise."
  [^org.symphonyoss.client.SymphonyClient connection ^org.symphonyoss.client.services.MessageListener listener]
  (.removeMessageListener (.getMessageService connection) listener))
