(defproject org.duct-framework/database.sql.hikaricp "0.7.1"
  :description "Integrant methods for a SQL database connection pool"
  :url "https://github.com/duct-framework/database.sql.hikaricp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [org.duct-framework/database.sql "0.4.1"]
                 [org.duct-framework/logger "0.4.0"]
                 [com.github.seancorfield/next.jdbc "1.3.1070"]
                 [com.zaxxer/HikariCP "7.0.2"]
                 [integrant "1.0.0"]
                 [net.ttddyy/datasource-proxy "1.11.0"]]
  :profiles
  {:dev {:dependencies [[org.xerial/sqlite-jdbc "3.50.3.0"]
                        [org.slf4j/slf4j-nop "2.0.17"]
                        [org.clojure/java.jdbc "0.7.12"]]}})
