(ns duct.database.sql.hikaricp-test
  (:require [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [clojure.test :refer :all]
            [duct.database.sql :as sql]
            [duct.database.sql.hikaricp :as hikaricp]
            [duct.logger :as log]
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
    (let [config   {::sql/hikaricp {:jdbc-url "jdbc:sqlite:"}}
          system   (ig/init config)
          hikaricp (::sql/hikaricp system)]
      (is (instance? duct.database.sql.Boundary hikaricp))
      (let [datasource (-> hikaricp :spec :datasource)]
        (is (instance? javax.sql.DataSource datasource))
        (is (not (closed? datasource)))
        (ig/halt! system)
        (is (closed? datasource)))))

  (testing "connection-uri"
    (let [config   {::sql/hikaricp {:connection-uri "jdbc:sqlite:"}}
          system   (ig/init config)
          hikaricp (::sql/hikaricp system)]
      (is (instance? duct.database.sql.Boundary hikaricp))
      (let [datasource (-> hikaricp :spec :datasource)]
        (is (instance? javax.sql.DataSource datasource))
        (is (not (closed? datasource)))
        (ig/halt! system)
        (is (closed? datasource)))))

  (testing "independent connection options"
    (let [config   {::sql/hikaricp {:adapter "sqlite"
                                    :database-name ""
                                    :username ""
                                    :password ""}}
          system   (ig/init config)
          hikaricp (::sql/hikaricp system)]
      (is (instance? duct.database.sql.Boundary hikaricp))
      (let [datasource (-> hikaricp :spec :datasource)]
        (is (instance? javax.sql.DataSource datasource))
        (is (not (closed? datasource)))
        (ig/halt! system)
        (is (closed? datasource))))))

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

(deftest logging-test
  (let [logs     (atom [])
        logger   (->AtomLogger logs)
        hikaricp (ig/init-key ::sql/hikaricp {:jdbc-url "jdbc:sqlite:" :logger logger})
        spec     (:spec hikaricp)]
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
           [[:info ::sql/query {:query ["CREATE TABLE foo (id INT, body TEXT)"]}]
            [:info ::sql/batch-query {:queries [["INSERT INTO foo VALUES (1, 'a')"]
                                                ["INSERT INTO foo VALUES (2, 'b')"]]}]
            [:info ::sql/query {:query ["SELECT * FROM foo"]}]
            [:info ::sql/query {:query ["SELECT * FROM foo WHERE id = ?" 1]}]
            [:info ::sql/query
             {:query ["SELECT * FROM foo WHERE id = ? AND body = ?" 1 "a"]}]]))
    (is (not (.isClosed (unwrap-logger (:datasource spec)))))
    (ig/halt-key! ::sql/hikaricp hikaricp)
    (is (closed? (:datasource spec)))))
