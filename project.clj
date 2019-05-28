;
; Copyright 2016 Fintech Open Source Foundation
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

(defproject org.symphonyoss/clj-symphony "1.0.1-SNAPSHOT"
  :description            "A Clojure wrapper around the symphony-java-client library."
  :url                    "https://github.com/symphonyoss/clj-symphony"
  :license                {:spdx-license-expression "Apache-2.0"
                           :name                    "Apache License, Version 2.0"
                           :url                     "http://www.apache.org/licenses/LICENSE-2.0"}
  :min-lein-version       "2.8.1"
  :plugins                [[lein-tools-deps "0.4.5"]]
  :middleware             [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:install :user :project]}
  :profiles               {:dev  {:plugins [[lein-licenses "0.2.2"]
                                            [lein-codox    "0.10.7"]
                                            [lein-nvd      "1.0.0"]]}
                            :1.8  {:lein-tools-deps/config {:aliases [:1.8]}}
                            :1.9  {:lein-tools-deps/config {:aliases [:1.9]}}
                            :1.10 {:lein-tools-deps/config {:aliases [:1.10]}}}
  :deploy-repositories    [["snapshots" {:url      "https://clojars.org/repo"
                                         :username :env/clojars_username
                                         :password :env/clojars_password}]
                           ["releases"  {:url      "https://clojars.org/repo"
                                         :username :env/clojars_username
                                         :password :env/clojars_password}]]
  :codox                  {:metadata {:doc/format :markdown}
                           :source-uri "https://github.com/symphonyoss/clj-symphony/blob/master/{filepath}#L{line}"})
