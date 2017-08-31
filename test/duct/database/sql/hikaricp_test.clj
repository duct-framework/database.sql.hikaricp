(ns duct.database.sql.hikaricp-test
  (:require [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [clojure.test :refer :all]
            [duct.core :as duct]
            [duct.database.sql :as sql]
            [duct.database.sql.hikaricp :as hikaricp]
            [duct.logger :as log]
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

(deftest execute-test
  (let [spec (:spec (ig/init-key ::sql/hikaricp {:jdbc-url "jdbc:sqlite:"}))]
    (jdbc/execute! spec ["CREATE TABLE foo (id INT)"])
    (jdbc/db-do-commands spec ["INSERT INTO foo VALUES (1)" "INSERT INTO foo VALUES (2)"])
    (is (= (jdbc/query spec ["SELECT * FROM foo"]) [{:id 1} {:id 2}]))))

(defrecord AtomLogger [a]
  duct.logger.Logger
  (-log [_ level _ _ _ _ event data]
    (swap! a conj [level event data])))

(defn- remove-elapsed [[level event data]]
  [level event (dissoc data :elapsed)])

(defn- elapsed [[_ _ {:keys [elapsed]}]]
  elapsed)

(defn- unwrap-logger [^javax.sql.DataSource datasource]
  (.unwrap datasource javax.sql.DataSource))

(deftest logging-test
  (let [logs     (atom [])
        logger   (->AtomLogger logs)
        hikaricp (ig/init-key ::sql/hikaricp {:jdbc-url "jdbc:sqlite:" :logger logger})
        spec     (:spec hikaricp)]
    (jdbc/execute! spec ["CREATE TABLE foo (id INT)"])
    (jdbc/db-do-commands spec ["INSERT INTO foo VALUES (1)" "INSERT INTO foo VALUES (2)"])
    (is (= (jdbc/query spec ["SELECT * FROM foo"]) [{:id 1} {:id 2}]))
    (is (every? nat-int? (map elapsed @logs)))
    (is (= (map remove-elapsed @logs)
           [[:info ::sql/query {:query "CREATE TABLE foo (id INT)"}]
            [:info ::sql/batch-query {:queries ["INSERT INTO foo VALUES (1)"
                                                     "INSERT INTO foo VALUES (2)"]}]
            [:info ::sql/query {:query "SELECT * FROM foo"}]]))
    (is (not (.isClosed (unwrap-logger (:datasource spec)))))
    (ig/halt-key! ::sql/hikaricp hikaricp)
    (is (.isClosed (unwrap-logger (:datasource spec))))))
