(defproject duct/database.sql.hikaricp "0.4.0"
  :description "Integrant methods for a SQL database connection pool"
  :url "https://github.com/duct-framework/database.sql.hikaricp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [duct/core "0.8.0"]
                 [duct/database.sql "0.1.0"]
                 [duct/logger "0.3.0"]
                 [hikari-cp "2.14.0"]
                 [integrant "0.8.0"]
                 [net.ttddyy/datasource-proxy "1.7"]]
  :profiles
  {:dev {:dependencies [[org.xerial/sqlite-jdbc "3.36.0.3"]
                        [org.slf4j/slf4j-nop "1.7.32"]
                        [org.clojure/java.jdbc "0.7.12"]]}})
