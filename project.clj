(defproject duct/database.sql.hikaricp "0.3.3"
  :description "Integrant methods for a SQL database connection pool"
  :url "https://github.com/duct-framework/database.sql.hikaricp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [duct/core "0.6.2"]
                 [duct/database.sql "0.1.0"]
                 [duct/logger "0.2.1"]
                 [hikari-cp "2.2.0"]
                 [integrant "0.6.3"]
                 [net.ttddyy/datasource-proxy "1.4.7"]]
  :profiles
  {:dev {:dependencies [[org.xerial/sqlite-jdbc "3.21.0.1"]
                        [org.slf4j/slf4j-nop "1.7.25"]
                        [org.clojure/java.jdbc "0.7.5"]]}})
