;
; Copyright 2016, 2017 Fintech Open Source Foundation
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

(defproject org.symphonyoss/clj-symphony "0.10.0-SNAPSHOT"
  :description         "A Clojure wrapper around the symphony-java-client library."
  :url                 "https://github.com/symphonyoss/clj-symphony"
  :license             {:spdx-license-expression "Apache-2.0"
                        :name                    "Apache License, Version 2.0"
                        :url                     "http://www.apache.org/licenses/LICENSE-2.0"}
  :min-lein-version    "2.8.1"
  :repositories        [["sonatype-snapshots" {:url "https://oss.sonatype.org/content/groups/public" :snapshots true}]
                        ["jitpack"            {:url "https://jitpack.io"}]]
  :dependencies        [[org.clojure/clojure                      "1.9.0"]
                        [org.symphonyoss.symphony/symphony-client "1.1.4"]
                        [org.apache.commons/commons-lang3         "3.8.1"]
                        [org.jsoup/jsoup                          "1.11.3"]
                        [cheshire                                 "5.8.1"]
                        [javax.xml.bind/jaxb-api                  "2.4.0-b180830.0359"]   ; Required as of JDK11
                        [org.glassfish.jaxb/jaxb-runtime          "2.4.0-b180830.0438"]]   ; Required as of JDK11
  :profiles            {:dev  {:plugins      [[lein-licenses "0.2.2"]
                                              [lein-codox    "0.10.4"]]}
                        :1.5  {:dependencies [[org.clojure/clojure "1.5.1"]]}
                        :1.6  {:dependencies [[org.clojure/clojure "1.6.0"]]}
                        :1.7  {:dependencies [[org.clojure/clojure "1.7.0"]]}
                        :1.8  {:dependencies [[org.clojure/clojure "1.8.0"]]}
                        :1.9  {:dependencies [[org.clojure/clojure "1.9.0"]]}
                        :1.10 {:dependencies [[org.clojure/clojure "1.10.0-master-SNAPSHOT"]]}}
  :deploy-repositories [["snapshots" {:url      "https://clojars.org/repo"
                                      :username :env/clojars_username
                                      :password :env/clojars_password}]
                        ["releases"  {:url      "https://clojars.org/repo"
                                      :username :env/clojars_username
                                      :password :env/clojars_password}]]
  :codox               {:metadata {:doc/format :markdown}
                        :source-uri "https://github.com/symphonyoss/clj-symphony/blob/master/{filepath}#L{line}"})
