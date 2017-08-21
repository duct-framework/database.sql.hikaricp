(ns duct.database.sql.hikaricp-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [duct.core :as duct]
            [duct.database.sql :as sql]
            [duct.database.sql.hikaricp :as hikaricp]
            [integrant.core :as ig]))

(duct/load-hierarchy)

(deftest connection-test
  (let [config   {::sql/hikaricp {:jdbc-url "jdbc:sqlite:"}}
        system   (ig/init config)
        hikaricp (::sql/hikaricp system)]
    (is (instance? duct.database.sql.Boundary hikaricp))
    (let [datasource (-> hikaricp :spec :datasource)]
      (is (instance? javax.sql.DataSource datasource))
      (is (not (.isClosed datasource)))
      (ig/halt! system)
      (is (.isClosed datasource)))))
