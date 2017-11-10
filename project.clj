(defproject duct/database.sql.hikaricp "0.3.1"
  :description "Integrant methods for a SQL database connection pool"
  :url "https://github.com/duct-framework/database.sql.hikaricp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-RC1"]
                 [duct/core "0.6.1"]
                 [duct/database.sql "0.1.0"]
                 [duct/logger "0.2.1"]
                 [hikari-cp "1.8.1"]
                 [integrant "0.6.1"]
                 [prismatic/schema "1.1.7"]
                 [net.ttddyy/datasource-proxy "1.4.4"]]
  :profiles
  {:dev {:dependencies [[org.xerial/sqlite-jdbc "3.20.1"]
                        [org.slf4j/slf4j-nop "1.7.25"]
                        [org.clojure/java.jdbc "0.7.3"]]}})
