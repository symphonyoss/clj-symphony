[![Build Status](https://travis-ci.org/symphonyoss/clj-symphony.svg?branch=master)](https://travis-ci.org/symphonyoss/clj-symphony)
[![Open Issues](https://img.shields.io/github/issues/symphonyoss/clj-symphony.svg)](https://github.com/symphonyoss/clj-symphony/issues)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/symphonyoss/clj-symphony.svg)](http://isitmaintained.com/project/symphonyoss/clj-symphony "Average time to resolve an issue")
[![License](https://img.shields.io/github/license/symphonyoss/clj-symphony.svg)](https://github.com/symphonyoss/clj-symphony/blob/master/LICENSE)
[![Dependencies Status](https://versions.deps.co/symphonyoss/clj-symphony/status.svg)](https://versions.deps.co/symphonyoss/clj-symphony)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/996/badge)](https://bestpractices.coreinfrastructure.org/projects/996)
[![FINOS - Released](https://cdn.jsdelivr.net/gh/finos/contrib-toolbox@master/images/badge-released.svg)](https://finosfoundation.atlassian.net/wiki/display/FINOS/Released)

<img align="right" width="40%" src="https://www.finos.org/hubfs/FINOS/finos-logo/FINOS_Icon_Wordmark_Name_RGB_horizontal.png">

# clj-symphony

This library is an idiomatic Clojure wrapper for the [symphony-java-client](https://github.com/symphonyoss/symphony-java-client)
library, a client binding and SDK for the [Symphony chat platform's REST API](https://rest-api.symphony.com/).

## Installation

clj-symphony is available as a Maven artifact from [Clojars](https://clojars.org/org.symphonyoss/clj-symphony).  The
latest released version is:

[![Clojars Project](https://img.shields.io/clojars/v/org.symphonyoss/clj-symphony.svg)](https://clojars.org/org.symphonyoss/clj-symphony)

### Trying it Out

If you prefer to kick the library's tyres without creating a project, you can use the [`lein try`
plugin](https://github.com/rkneufeld/lein-try):

```shell
$ lein try org.symphonyoss/clj-symphony
```

or (as of v0.10.0), if you have installed the [Clojure CLI
tools](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools):

```shell
$ clj -Sdeps '{:deps {org.symphonyoss/clj-symphony {:mvn/version "#.#.#"}}}'  # Where #.#.# is replaced with an actual version number >= 0.10.0
```

Either way, you will be dropped in a REPL with the library downloaded and ready for use.

## Usage

The functionality is provided by several `clj-symphony._____` namespaces.

Require them in the REPL:

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

## Roadmap

This project's roadmap is managed exclusively via [milestones in the project's GitHub issue tracker](https://github.com/symphonyoss/clj-symphony/milestones?direction=asc&sort=due_date).  Typically there will be two milestones that are being tracked at most points in time:

1. The next maintenance (patch) release.
2. The next feature (minor or major) release.

This project strictly follows [semantic versioning rules](https://semver.org/#summary) for determining how issues are assigned to each of these milestones, and what impact that will have on the release's version number (including whether a feature release is major or minor, depending on whether backwards compatibility is broken or not).

## Tested Versions

clj-symphony is [tested on](https://travis-ci.org/symphonyoss/clj-symphony):

|                           | JVM v1.7         | JVM v1.8       | JVM v9         | JVM v10         | JVM v11        |
|                      ---: | :---:            | :---:          |  :---:         |  :---:          |  :---:         |
| Clojure 1.7.0             | ❌<sup>1,2</sup> | ❌<sup>1</sup> | ❌<sup>1</sup> | ❌<sup>1</sup> | ❌<sup>1</sup> |
| Clojure 1.8.0             | ❌<sup>2</sup>   | ✅             | ✅             | ✅             | ✅             |
| Clojure 1.9.0             | ❌<sup>2</sup>   | ✅             | ✅             | ✅             | ✅             |
| Clojure 1.10.0 (snapshot) | ❌<sup>2</sup>   | ✅             | ✅             | ✅             | ✅             |

<sup>1</sup> clj-symphony only supports Clojure 1.8 and up

<sup>2</sup> [symphony-java-client](https://github.com/symphonyoss/symphony-java-client) only supports JVM v1.8 and up

## Contributor Information

[Contributing Guidelines](https://github.com/symphonyoss/bot-unfurl/blob/master/.github/CONTRIBUTING.md)

[GitHub project](https://github.com/symphonyoss/clj-symphony)

[Bug Tracker](https://github.com/symphonyoss/clj-symphony/issues)

## License

Copyright 2016 Fintech Open Source Foundation

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
