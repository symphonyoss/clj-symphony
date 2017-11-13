[![Build Status](https://travis-ci.org/symphonyoss/clj-symphony.svg?branch=master)](https://travis-ci.org/symphonyoss/clj-symphony)
[![Open Issues](https://img.shields.io/github/issues/symphonyoss/clj-symphony.svg)](https://github.com/symphonyoss/clj-symphony/issues)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/symphonyoss/clj-symphony.svg)](http://isitmaintained.com/project/symphonyoss/clj-symphony "Average time to resolve an issue")
[![License](https://img.shields.io/github/license/symphonyoss/clj-symphony.svg)](https://github.com/symphonyoss/clj-symphony/blob/master/LICENSE)
[![Dependencies Status](https://versions.deps.co/symphonyoss/clj-symphony/status.svg)](https://versions.deps.co/symphonyoss/clj-symphony)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/996/badge)](https://bestpractices.coreinfrastructure.org/projects/996)
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
$ lein try org.symphonyoss/clj-symphony 0.2.0-SNAPSHOT
```

You will be dropped in a REPL with the library downloaded and ready for use.

## Usage

The functionality is provided by several `clj-symphony._____` namespaces.

Require them in the REPL (incl. a `lein try` REPL):

```clojure
(require '[clj-symphony.connect         :as syc]  :reload-all)
(require '[clj-symphony.user            :as syu]  :reload-all)
(require '[clj-symphony.stream          :as sys]  :reload-all)
(require '[clj-symphony.chat            :as sych] :reload-all)
(require '[clj-symphony.room            :as syrm] :reload-all)
(require '[clj-symphony.message         :as sym]  :reload-all)
(require '[clj-symphony.user-connection :as syuc] :reload-all)
```

Require them in your project:

```clojure
(ns my-app.core
  (:require [clj-symphony.connect         :as syc]
            [clj-symphony.user            :as syu]
            [clj-symphony.stream          :as sys]
            [clj-symphony.chat            :as sych]
            [clj-symphony.room            :as syrm]
            [clj-symphony.message         :as sym]
            [clj-symphony.user-connection :as syuc]))
```

[Detailed API documentation is published here](https://symphonyoss.github.io/clj-symphony/).

## Contributor Information

[GitHub project](https://github.com/symphonyoss/clj-symphony)

[Bug Tracker](https://github.com/symphonyoss/clj-symphony/issues)

## License

Copyright © 2016, 2017 Symphony Software Foundation

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
