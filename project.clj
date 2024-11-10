(defproject org.duct-framework/database.sql.hikaricp "0.7.0"
  :description "Integrant methods for a SQL database connection pool"
  :url "https://github.com/duct-framework/database.sql.hikaricp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.4"]
                 [org.duct-framework/database.sql "0.4.0"]
                 [org.duct-framework/logger "0.4.0"]
                 [com.github.seancorfield/next.jdbc "1.3.955"]
                 [com.zaxxer/HikariCP "6.1.0"]
                 [integrant "0.13.1"]
                 [net.ttddyy/datasource-proxy "1.10"]]
  :profiles
  {:dev {:dependencies [[org.xerial/sqlite-jdbc "3.47.0.0"]
                        [org.slf4j/slf4j-nop "2.0.16"]
                        [org.clojure/java.jdbc "0.7.12"]]}})
