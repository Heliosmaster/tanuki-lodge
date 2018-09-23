(defproject tanuki-lodge "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repl-options {:init-ns tanuki-lodge.core}
  :profiles {:uberjar {:main tanuki-lodge.core
                       :omit-source true
                       :aot :all
                       :source-paths ["src"]
                       :uberjar-name "tanuki-lodge.jar"}}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [compojure "1.6.1"]
                 [ring "1.6.1"]
                 [clj-http "3.9.1"]
                 [cheshire "5.8.1"]
                 [ring/ring-json "0.4.0"]
                 [camel-snake-kebab "0.4.0"]
                 [environ "1.1.0"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.9"]
                 [mount "0.1.13"]])
