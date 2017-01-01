[![Build Status](https://travis-ci.org/symphonyoss/clj-symphony.svg?branch=master)](https://travis-ci.org/symphonyoss/clj-symphony)
[![Open Issues](https://img.shields.io/github/issues/symphonyoss/clj-symphony.svg)](https://github.com/symphonyoss/clj-symphony/issues)
[![License](https://img.shields.io/github/license/symphonyoss/clj-symphony.svg)](https://github.com/symphonyoss/clj-symphony/blob/master/LICENSE)
[![Dependencies Status](http://jarkeeper.com/symphonyoss/clj-symphony/status.svg)](http://jarkeeper.com/symphonyoss/clj-symphony)
[![Symphony Software Foundation - Incubating](https://cdn.rawgit.com/symphonyoss/contrib-toolbox/master/images/ssf-badge-incubating.svg)](https://symphonyoss.atlassian.net/wiki/display/FM/Project+lifecycle)

# clj-symphony

This WIP library is intended to be an idiomatic Clojure wrapper for the [symphony-java-client](https://github.com/symphonyoss/symphony-java-client) library.

## Installation

clj-symphony is available as a Maven artifact from [Clojars](https://clojars.org/org.symphonyoss/clj-symphony).
Plonk the following in your project.clj :dependencies, `lein deps` and you should be good to go:

```clojure
[org.symphonyoss/clj-symphony "#.#.#"]   ; Where #.#.# is replaced with an actual version number
```

The latest version is:

[![Clojars Project](https://img.shields.io/clojars/v/org.symphonyoss/clj-symphony.svg)](https://clojars.org/org.symphonyoss/clj-symphony)

## Usage

The functionality is provided by the `clj-symphony.api` namespace.

Require it in the REPL:

```clojure
(require '[clj-symphony.api :as symph] :reload-all)
```

Require it in your application:

```clojure
(ns my-app.core
  (:require [clj-symphony.api :as symph]))
```

The library provides a number of methods:

```
user=> (require '[clj-symphony.api :as symph] :reload-all)
nil
user=> (doc clj-symphony.api/connect)
-------------------------
clj-symphony.api/connect
([params])
  Connect to a Symphony pod as a given service account user.  Returns a 'session' object
  that should be used in all subsequent API calls.

  params is a map containing:
  :pod-id           The id of the pod to connect to - will autopopulate whichever of the 4 URLs aren't provided. (optional - see below)
  :session-auth-url The URL of the session authentication endpoint. (optional - see below)
  :key-auth-url     The URL of the key authentication endpoint. (optional - see below)
  :agent-api-url    The URL of the agent API. (optional - see below)
  :pod-api-url      The URL of the Pod API. (optional - see below)
  :trust-store      A pair of strings containing the path to the trust store and the password of the trust store. (mandatory)
  :user-cert        A pair of strings containing the path to the bot user's certificate and the password of that certificate. (mandatory)
  :user-email       The email address of the bot user. (mandatory)

  Note: if :pod-id is not provided, :session-auth-url and :key-auth-url and :agent-api-url and :pod-api-url are all mandatory.
nil
user=> (doc clj-symphony.api/user-info)
-------------------------
clj-symphony.api/user-info
([session] [session user-identifier])
  Returns a map containing information about the given user, or the authenticated session user if a user id is not provided.
  User can be specified either as a user id (Long) or an email address (String).

  Returns nil if the user doesn't exist.

  Note: providing a user identifier requires calls to the server.
nil
user=> (doc clj-symphony.api/user-presence)
-------------------------
clj-symphony.api/user-presence
([session] [session user-id])
  Returns the presence status of the given user, or all users.
nil
user=> (doc clj-symphony.api/get-chats)
-------------------------
clj-symphony.api/get-chats
([session] [session user-id])
  Returns a list of chats for the given user, or for the authenticated session user if a user id is not provided.
nil
user=> (doc clj-symphony.api/establish-chat)
-------------------------
clj-symphony.api/establish-chat
([session user-identifier])
  Establishes a chat with the given user.
nil
user=> (doc clj-symphony.api/send-message!)
-------------------------
clj-symphony.api/send-message!
  Sends a message to the given chat, room or stream.  Both text and MessageML messages are supported.
nil
user=> (doc clj-symphony.api/register-message-listener)
-------------------------
clj-symphony.api/register-message-listener
([session f])
  Registers f, a function of 7 parameters, as a message listener (callback), and returns a handle to that listener
  so that it can be deregistered later on, if needed.  The 7 arguments passed to f are:

     msg-id     - Identifier of the message
     timestamp  - Timestamp the message was sent (as a string in ####???? format)
     stream-id  - Identifier of the stream (chat or room) the message was sent to)
     user-id    - Identifier of the user who sent the message
     msg-format - Format of the message as a keyword (:messageml or :text)
     msg-type   - ####????
     msg        - Text of the message

   The value returned by f (if any) is ignored.
nil
user=> (doc clj-symphony.api/deregister-message-listener)
-------------------------
clj-symphony.api/deregister-message-listener
([session listener])
  Deregisters a previously-registered message listener.  Once deregistered, a listener should be discarded.
  Returns true if a valid message listener was deregistered, false otherwise.
nil
```

## Developer Information

[GitHub project](https://github.com/symphonyoss/clj-symphony)

[Bug Tracker](https://github.com/symphonyoss/clj-symphony/issues)

## License

Copyright © 2016 Symphony Software Foundation

Distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

### 3rd Party Licenses

To see the list of licenses of the third party libraries used by this project, please run:

```shell
$ lein licenses :csv | cut -d , -f3 | sort | uniq
```

To see the dependencies and licenses in detail, run:

```shell
$ lein licenses
```
