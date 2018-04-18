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

(defproject org.symphonyoss/clj-symphony "0.8.0-SNAPSHOT"
  :description         "A Clojure wrapper around the symphony-java-client library."
  :url                 "https://github.com/symphonyoss/clj-symphony"
  :license             {:spdx-license-expression "Apache-2.0"
                        :name                    "Apache License, Version 2.0"
                        :url                     "http://www.apache.org/licenses/LICENSE-2.0"}
  :min-lein-version    "2.8.1"
  :repositories        [
                         ["sonatype-snapshots" {:url "https://oss.sonatype.org/content/groups/public" :snapshots true}]
                         ["jitpack"            {:url "https://jitpack.io"                             :snapshots true}]
                       ]
  :plugins             [
                         [lein-codox "0.10.3"]
                       ]
  :dependencies        [
                         [org.clojure/clojure                      "1.9.0"]
                         [org.symphonyoss.symphony/symphony-client "1.1.4"]
                         [org.apache.commons/commons-lang3         "3.7"]
                         [org.jsoup/jsoup                          "1.11.3"]
                         [cheshire                                 "5.8.0"]
                       ]
  :profiles            {
                         :dev     {:plugins [[lein-licenses "0.2.2"]]}
                         :uberjar {:aot :all}
                       }
  :jvm-opts            ~(let [version     (System/getProperty "java.version")
                              [major _ _] (clojure.string/split version #"\.")]
                          (if (>= (java.lang.Integer/parseInt major) 9)
                            ["--add-modules" "java.xml.bind"]
                            []))
  :deploy-repositories [
                         ["snapshots" {:url      "https://clojars.org/repo"
                                       :username :env/clojars_username
                                       :password :env/clojars_password}]
                         ["releases"  {:url      "https://clojars.org/repo"
                                       :username :env/clojars_username
                                       :password :env/clojars_password}]
                       ]
  :codox               {
                         :metadata {:doc/format :markdown}
                         :source-uri "https://github.com/symphonyoss/clj-symphony/blob/master/{filepath}#L{line}"
                       })
