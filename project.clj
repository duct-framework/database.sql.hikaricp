(defproject duct/database.sql.hikaricp "0.4.0"
  :description "Integrant methods for a SQL database connection pool"
  :url "https://github.com/duct-framework/database.sql.hikaricp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.4"]
                 [org.duct-framework/database.sql "0.2.0"]
                 [org.duct-framework/logger "0.3.0"]
                 [hikari-cp "3.0.1"]
                 [integrant "0.13.1"]
                 [net.ttddyy/datasource-proxy "1.7"]]
  :profiles
  {:dev {:dependencies [[org.xerial/sqlite-jdbc "3.36.0.3"]
                        [org.slf4j/slf4j-nop "1.7.32"]
                        [org.clojure/java.jdbc "0.7.12"]]}})
