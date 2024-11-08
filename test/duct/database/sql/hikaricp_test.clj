(ns duct.database.sql.hikaricp-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.test :refer [deftest is testing]]
            [duct.database.sql :as sql]
            [duct.database.sql.hikaricp :as hikaricp]
            [integrant.core :as ig]))

(ig/load-hierarchy)

(defn- unwrap-logger [^javax.sql.DataSource datasource]
  (if (.isWrapperFor datasource com.zaxxer.hikari.HikariDataSource)
    (.unwrap datasource com.zaxxer.hikari.HikariDataSource)
    datasource))

(defn- closed? [datasource]
  (.isClosed (unwrap-logger datasource)))

(deftest connection-test
  (testing "jdbc-url"
    (let [config   {::sql/hikaricp {:jdbcUrl "jdbc:sqlite:"}}
          system   (ig/init config)
          hikaricp (::sql/hikaricp system)]
      (is (instance? duct.database.sql.Boundary hikaricp))
      (let [datasource (:datasource hikaricp)]
        (is (instance? javax.sql.DataSource datasource))
        (is (not (closed? datasource)))
        (ig/halt! system)
        (is (closed? datasource))))))

(deftest execute-test
  (let [spec (ig/init-key ::sql/hikaricp {:jdbcUrl "jdbc:sqlite:"})]
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

(deftest logging-test
  (let [logs   (atom [])
        logger (->AtomLogger logs)
        spec   (ig/init-key ::sql/hikaricp {:jdbcUrl "jdbc:sqlite:" :logger logger})]
    (jdbc/execute! spec ["CREATE TABLE foo (id INT, body TEXT)"])
    (jdbc/db-do-commands spec ["INSERT INTO foo VALUES (1, 'a')"
                               "INSERT INTO foo VALUES (2, 'b')"])
    (is (= (jdbc/query spec ["SELECT * FROM foo"])
           [{:id 1, :body "a"} {:id 2, :body "b"}]))
    (is (= (jdbc/query spec ["SELECT * FROM foo WHERE id = ?" 1])
           [{:id 1, :body "a"}]))
    (is (= (jdbc/query spec ["SELECT * FROM foo WHERE id = ? AND body = ?" 1 "a"])
           [{:id 1, :body "a"}]))
    (is (every? nat-int? (map elapsed @logs)))
    (is (= (map remove-elapsed @logs)
           [[:info ::sql/query {:sql [["CREATE TABLE foo (id INT, body TEXT)"]]}]
            [:info ::sql/query {:sql [["INSERT INTO foo VALUES (1, 'a')"]
                                      ["INSERT INTO foo VALUES (2, 'b')"]]}]
            [:info ::sql/query {:sql [["SELECT * FROM foo"]]}]
            [:info ::sql/query {:sql [["SELECT * FROM foo WHERE id = ?" 1]]}]
            [:info ::sql/query
             {:sql [["SELECT * FROM foo WHERE id = ? AND body = ?" 1 "a"]]}]]))
    (is (not (.isClosed (unwrap-logger (:datasource spec)))))
    (ig/halt-key! ::sql/hikaricp spec)
    (is (closed? (:datasource spec)))))
