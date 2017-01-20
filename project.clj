;
; Copyright © 2016 Symphony Software Foundation
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

(defproject org.symphonyoss/clj-symphony "0.1.0-SNAPSHOT"
  :description         "A Clojure wrapper around the symphony-java-client library."
  :url                 "https://github.com/symphonyoss/clj-symphony"
  :license             {:name "Apache License, Version 2.0"
                        :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :min-lein-version    "2.5.0"
  :repositories     [["sonatype-snapshots" {:url "https://oss.sonatype.org/content/groups/public" :snapshots true}]
                     ["jitpack"            {:url "https://jitpack.io"}]]
  :plugins             [[lein-licenses     "0.2.1"]
                        [lein-codox        "0.10.2"]
;                        [katlex/github-cdn "0.1.4-SNAPSHOT"]                 ; Once https://github.com/katlex/github-cdn/pull/1 is merged and released
                        [com.github.pmonks/github-cdn "-SNAPSHOT"]]   ; Until https://github.com/katlex/github-cdn/pull/1 is merged and released
  :dependencies        [
                         [org.clojure/clojure                      "1.8.0"]
                         [org.symphonyoss.symphony/symphony-client "1.0.0-SNAPSHOT"]
                       ]
  :profiles            {:dev {:dependencies [[midje      "1.8.3"]]
                              :plugins      [[lein-midje "3.2"]]}   ; Don't remove this or travis-ci will assplode!
                        :uberjar {:aot :all}}
  :deploy-repositories [
                         ["snapshots" {:url "https://clojars.org/repo"
                                       :username :env/clojars_username
                                       :password :env/clojars_password}]
                         ["releases"  {:url "https://clojars.org/repo"
                                       :username :env/clojars_username
                                       :password :env/clojars_password}]
                       ]
  :github-cdn          {:dir "target/doc/"})
