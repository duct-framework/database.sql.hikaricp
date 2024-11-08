(ns duct.database.sql.hikaricp
  (:require [duct.database.sql :as sql]
            [duct.logger :as log]
            [integrant.core :as ig]
            [next.jdbc.connection :as conn])
  (:import [javax.sql DataSource]
           [com.zaxxer.hikari HikariDataSource]
           [net.ttddyy.dsproxy QueryInfo]
           [net.ttddyy.dsproxy.support ProxyDataSource]
           [net.ttddyy.dsproxy.listener QueryExecutionListener]))

(defn- query-parameters [params]
  (->> params (map (memfn getArgs)) (sort-by #(aget % 0)) (mapv #(aget % 1))))

(defn- query-parameter-lists [^QueryInfo query-info]
  (mapv query-parameters (.getParametersList query-info)))

(defn- logged-query [^QueryInfo query-info]
  (let [query  (.getQuery query-info)
        params (query-parameter-lists query-info)]
    (into [query] (if (= (count params) 1) (first params) params))))

(defn- logging-listener [logger]
  (reify QueryExecutionListener
    (beforeQuery [_ _ _])
    (afterQuery [_ exec-info query-infos]
      (log/info logger ::sql/query {:sql     (mapv logged-query query-infos)
                                    :elapsed (.getElapsedTime exec-info)}))))

(defn- wrap-logger [datasource logger]
  (doto (ProxyDataSource. datasource)
    (.addListener (logging-listener logger))))

(defn- unwrap-logger [^DataSource datasource]
  (if (.isWrapperFor datasource HikariDataSource)
    (.unwrap datasource HikariDataSource)
    datasource))

(defmethod ig/init-key :duct.database.sql/hikaricp
  [_ {:keys [logger] :as options}]
  (sql/->Boundary (-> (dissoc options :logger)
                      (as-> cfg (conn/->pool HikariDataSource cfg))
                      (cond-> logger (wrap-logger logger)))))

(defmethod ig/halt-key! :duct.database.sql/hikaricp [_ {:keys [datasource]}]
  (.close ^HikariDataSource (unwrap-logger datasource)))
