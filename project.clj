(defproject duct/database.sql.hikaricp "0.1.2"
  :description "Integrant methods for a SQL database connection pool"
  :url "https://github.com/duct-framework/database.sql.hikaricp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [duct/core "0.6.0"]
                 [duct/database.sql "0.1.0"]
                 [hikari-cp "1.7.5"]
                 [integrant "0.6.1"]
                 [prismatic/schema "1.1.6"]]
  :profiles
  {:dev {:dependencies [[org.xerial/sqlite-jdbc "3.16.1"]
                        [org.slf4j/slf4j-nop "1.7.21"]]}})
