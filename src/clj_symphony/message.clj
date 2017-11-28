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
  "Operations related to MessageMLv2 messages, which combine one or more of:

   1. a human-readable message (formatted using a subset of HTML called 'PresentationML')
   2. a machine readable JSON blob called the 'entity data'
   3. an attachment

  See:

   * https://rest-api.symphony.com/docs/messagemlv2 for details on PresentationML's formatting capabilities
   * https://rest-api.symphony.com/docs/objects for details on MessageMLv2's entity data capabilities"
  (:require [clojure.string      :as s]
            [clojure.java.io     :as io]
            [cheshire.core       :as ch]
            [clj-symphony.user   :as syu]
            [clj-symphony.stream :as sys]))


(defn- parse-entity-data
  "Parses the given entity data JSON string into a Clojure data structure."
  [^String ed]
  (if-not (s/blank? ed)
    (ch/parse-string ed)))


(defn msgobj->map
  "Converts a `org.symphonyoss.symphony.clients.model.SymMessage` object into a
  map with these keys:

  | Key              | Description                                                      |
  |------------------|------------------------------------------------------------------|
  | `:message-id`    | The id of the message.                                           |
  | `:timestamp`     | The timestamp of the message.                                    |
  | `:stream-id`     | The stream id of the stream the message was sent to.             |
  | `:user-id`       | The user id of the user who sent the message.                    |
  | `:type`          | The 'type' of the message. *(seems to be unused??)*              |
  | `:text`          | The MessageMLv2 'text' of the message.                           |
  | `:attachment`    | The attachment for the message (if any).                         |
  | `:entity-data`   | The parsed JSON 'entity data' for the message as a map (if any). |
  "
  [^org.symphonyoss.symphony.clients.model.SymMessage m]
  (if m
    {
      :message-id  (.getId          m)
      :timestamp   (java.util.Date. (Long/valueOf (.getTimestamp m)))
      :stream-id   (.getStreamId    m)
      :user-id     (.getFromUserId  m)
      :type        (.getMessageType m)   ; This seems to be null or blank most of the time...
      :text        (.getMessage     m)
      :attachment  (.getAttachment  m)
      :entity-data (parse-entity-data (.getEntityData m))
    }))


(defmulti mentions
  "Returns the list of user ids mentioned in the message (or nil if there aren't any)."
  {:arglists '([m])}
  type)

(defmethod mentions nil
  [m]
  nil)

(defmethod mentions java.util.Map
  [{:keys [entity-data]}]
  (if entity-data
    (seq
      (map #(Long/parseLong (get (first (get (second %) "id")) "value"))
           (filter #(= "com.symphony.user.mention" (get (val %) "type")) entity-data)))))

(defmethod mentions org.symphonyoss.symphony.clients.model.SymMessage
  [^org.symphonyoss.symphony.clients.model.SymMessage m]
  (mentions { :entity-data (parse-entity-data (.getEntityData m)) }))


(defn escape
  "Escapes the given string as content in a MessageMLv2 message."
  [^String m]
  (if m
    (org.apache.commons.lang3.StringEscapeUtils/escapeXml11 m)))


(def ^:private charset-utf8   (java.nio.charset.Charset/forName "UTF-8"))
(def ^:private jsoup-settings (doto (org.jsoup.nodes.Document$OutputSettings.)
                                (.prettyPrint false)))

(defn to-plain-text
  "Converts a MessageML message to plain text, by stripping most tags,
  converting `<p>` and `<br/>` tags into newlines, and unescaping HTML entities
  into their Unicode equivalents."
  [^String m]
  (if m
    (let [doc (doto (org.jsoup.Jsoup/parseBodyFragment m)
                (.outputSettings jsoup-settings)
                (.charset charset-utf8))
          _   (.append (.select doc "br") "\n")
          _   (.prepend (.select doc "p") "\n\n")
          tmp (.html doc)]
      (s/replace
        (org.jsoup.parser.Parser/unescapeEntities
          (org.jsoup.Jsoup/clean tmp
                                 ""
                                 (org.jsoup.safety.Whitelist/none)
                                 jsoup-settings)
          false)
        "\u00A0"  ; Unicode non-breaking space character (i.e. &nbsp;)
        " "))))


(defn tokens
  "Returns the tokens (words) in the given MessageML message, in all lower case.  Splits on whitespace and punctuation
  (as per the the Java regex pattern '\\p{Punct}').  Note that message content that contains whitespace within a single
  tag will also be split - this is especially noticable for @mentions containing a person's first and last names."
  [^String m]
  (if m
    (s/split (s/lower-case (to-plain-text m)) #"[\p{Punct}\s]+")))


(defn send-message!
   "Sends a message to the stream `s` (which can be anything supported by [[clj-symphony.stream/stream-id]]).

    * `m` is a String containing MessageMLv2
    * `ed` is a map or a JSON String containing entity data (a map will be converted to a String)
    * `a` is an attachment (something compatible with [clojure.java.io/file](https://clojure.github.io/clojure/clojure.java.io-api.html))
   "
  ([c s m]    (send-message! c s m nil nil))
  ([c s m ed] (send-message! c s m ed nil))
  ([^org.symphonyoss.client.SymphonyClient c s ^String m ed a]
    (let [stream-id (sys/stream-id s)
          ed-str    (if (instance? java.util.Map ed)
                      (ch/generate-string ed)
                      ed)]
      (.sendMessage (.getMessagesClient c)
                    (doto (org.symphonyoss.symphony.pod.model.Stream.)
                      (.setId stream-id))
                    (doto
                      (org.symphonyoss.symphony.clients.model.SymMessage.)
                      (.setStreamId   stream-id)
                      (.setMessage    m)
                      (.setEntityData ed-str)
                      (.setAttachment (io/file a)))))
    nil))


(defn register-listener!
  "Registers f, a function with 1 parameter, as a message listener (callback),
  and returns a handle to that listener so that it can be deregistered later on,
  if needed.  Listeners registered in this manner are not scoped to any
  particular stream - they will be sent all messages from all streams that the
  authenticated connection user is a participant in.

  The argument passed to f is a map generated by [[msgobj->map]].

  The value returned by f (if any) is ignored."
  [^org.symphonyoss.client.SymphonyClient c f]
  (let [listener (reify
                   org.symphonyoss.client.services.MessageListener
                   (onMessage [this msg]
                     (f (msgobj->map msg))))]
    (.addMessageListener (.getMessageService c) listener)
    listener))


(defn deregister-listener!
  "Deregisters a previously-registered message listener.  Once deregistered, a
  listener should be discarded (they cannot be reused). Returns `true` if a
  valid message listener was deregistered, `false` otherwise."
  [^org.symphonyoss.client.SymphonyClient c ^org.symphonyoss.client.services.MessageListener l]
  (.removeMessageListener (.getMessageService c) l))
