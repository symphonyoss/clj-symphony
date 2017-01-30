[![Build Status](https://travis-ci.org/symphonyoss/clj-symphony.svg?branch=master)](https://travis-ci.org/symphonyoss/clj-symphony)
[![Open Issues](https://img.shields.io/github/issues/symphonyoss/clj-symphony.svg)](https://github.com/symphonyoss/clj-symphony/issues)
[![License](https://img.shields.io/github/license/symphonyoss/clj-symphony.svg)](https://github.com/symphonyoss/clj-symphony/blob/master/LICENSE)
[![Dependency Status (JarKeeper)](http://jarkeeper.com/symphonyoss/clj-symphony/status.svg)](http://jarkeeper.com/symphonyoss/clj-symphony)
[![Dependency Status (VersionEye)](https://www.versioneye.com/user/projects/588f210f683c11004fc6c8d7/badge.svg?style=flat-round)](https://www.versioneye.com/user/projects/588f210f683c11004fc6c8d7)
[![Symphony Software Foundation - Incubating](https://cdn.rawgit.com/symphonyoss/contrib-toolbox/master/images/ssf-badge-incubating.svg)](https://symphonyoss.atlassian.net/wiki/display/FM/Incubating)

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

### Trying it Out
Alternatively, you may prefer to kick the library's tyres without creating a project.  This is a snap with the awesome [`lein try` plugin](https://github.com/rkneufeld/lein-try):

```shell
$ lein try org.symphonyoss/clj-symphony 0.1.0-SNAPSHOT
```

You will be dropped in a REPL with the library downloaded and ready for use.

## Usage

The functionality is provided by the `clj-symphony.api` namespace.

Require it in the REPL (incl. a `lein try` REPL):

```clojure
(require '[clj-symphony.api :as symph] :reload-all)
```

Require it in your project:

```clojure
(ns my-app.core
  (:require [clj-symphony.api :as symph]))
```

[Detailed API documentation is published here](https://symphonyoss.github.io/clj-symphony/).

## Contributor Information

[GitHub project](https://github.com/symphonyoss/clj-symphony)

[Bug Tracker](https://github.com/symphonyoss/clj-symphony/issues)

## License

Copyright © 2016 Symphony Software Foundation

Distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

### 3rd Party Licenses

To see the full list of licenses of all third party libraries used by this project, please run:

```shell
$ lein licenses :csv | cut -d , -f3 | sort | uniq
```

To see the dependencies and licenses in detail, run:

```shell
$ lein licenses
```

This project depends on the following libraries, which are licensed under Common Development and Distribution License 1.0, 1.1 or 2.0.  For details, please see each individuals library's page.

* [javax.annotation/javax.annotation-api](https://mvnrepository.com/artifact/javax.annotation/javax.annotation-api)
* [javax.ws.rs/javax.ws.rs-api](https://mvnrepository.com/artifact/javax.ws.rs/javax.ws.rs-api)
* [org.glassfish.hk2.external/aopalliance-repackaged](https://mvnrepository.com/artifact/org.glassfish.hk2.external/aopalliance-repackaged)
* [org.glassfish.hk2.external/javax.inject](https://mvnrepository.com/artifact/org.glassfish.hk2.external/javax.inject)
* [org.glassfish.hk2/hk2-api](https://mvnrepository.com/artifact/org.glassfish.hk2/hk2-api)
* [org.glassfish.hk2/hk2-locator](https://mvnrepository.com/artifact/org.glassfish.hk2/hk2-locator)
* [org.glassfish.hk2/hk2-utils](https://mvnrepository.com/artifact/org.glassfish.hk2/hk2-utils)
* [org.glassfish.hk2/osgi-resource-locator](https://mvnrepository.com/artifact/org.glassfish.hk2/osgi-resource-locator)
* [org.glassfish.jersey.bundles.repackaged/jersey-guava](https://mvnrepository.com/artifact/org.glassfish.jersey.bundles.repackaged/jersey-guava)
* [org.glassfish.jersey.core/jersey-client](https://mvnrepository.com/artifact/org.glassfish.jersey.core/jersey-client)
* [org.glassfish.jersey.core/jersey-common](https://mvnrepository.com/artifact/org.glassfish.jersey.core/jersey-common)
* [org.glassfish.jersey.ext/jersey-entity-filtering](https://mvnrepository.com/artifact/org.glassfish.jersey.ext/jersey-entity-filtering)
* [org.glassfish.jersey.media/jersey-media-json-jackson](https://mvnrepository.com/artifact/org.glassfish.jersey.media/jersey-media-json-jackson)
* [org.glassfish.jersey.media/jersey-media-multipart](https://mvnrepository.com/artifact/org.glassfish.jersey.media/jersey-media-multipart)
* [org.jvnet.mimepull/mimepull](https://mvnrepository.com/artifact/org.jvnet.mimepull/mimepull)

This project depends on the following libraries, which are licensed under Eclipse Public License 1.0.  For details, please see each individuals library's page.

* [clojure-complete](https://github.com/ninjudd/clojure-complete)
* [colorize](https://github.com/ibdknox/colorize)
* [environ](https://github.com/weavejester/environ)
* [flare](https://github.com/andersfurseth/flare)
* [junit](http://junit.org/junit4/)
* [ordered](https://github.com/amalloy/ordered)
* [org.clojure/clojure](https://github.com/clojure/clojure)
* [org.clojure/core.unify](https://github.com/clojure/core.unify)
* [org.clojure/math.combinatorics](https://github.com/clojure/math.combinatorics)
* [org.clojure/tools.macro](https://github.com/clojure/tools.macro)
* [org.clojure/tools.namespace](https://github.com/clojure/tools.namespace)
* [org.clojure/tools.nrepl](https://github.com/clojure/tools.nrepl)
* [org.tcrawley/dynapath](https://github.com/tobias/dynapath)
* [slingshot](https://github.com/scgilardi/slingshot)
* [swiss-arrows](https://github.com/rplevy/swiss-arrows)

