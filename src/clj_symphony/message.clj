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

(ns clj-symphony.message
  "Operations related to messages.  Currently, Symphony supports these message formats:
  1. plain text
  2. messageML, which supports a small number of formatting tags
  3. MessageMLv2 (coming soon - stay tuned!)"
  (:require [clj-symphony.user   :as syu]
            [clj-symphony.stream :as sys]
            [clj-symphony.chat   :as sych]))

(defn- build-sym-message
  [^String message]
  (let [msg (doto
              (org.symphonyoss.symphony.clients.model.SymMessage.)
              (.setMessage message))]
    (if (.startsWith message "<messageML>")
      (.setFormat msg org.symphonyoss.symphony.clients.model.SymMessage$Format/MESSAGEML)
      (.setFormat msg org.symphonyoss.symphony.clients.model.SymMessage$Format/TEXT))
    msg))


(defmulti send-message!
  "Sends the given message to the given target (chat, room, or stream)."
  (fn [connection target message] (type target)))

(defmethod send-message! nil
  [connection target message]
  nil)

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

(defmethod send-message! java.util.Map
  [connection {:keys [stream-id]} message]
  (if stream-id
    (send-message! connection stream-id message)))
