(ns duct.database.sql.hikaricp
  (:require [integrant.core :as ig]
            [duct.database.sql :as sql]
            [duct.logger :as log]
            [hikari-cp.core :as hikari-cp])
  (:import [javax.sql DataSource]
           [net.ttddyy.dsproxy QueryInfo]
           [net.ttddyy.dsproxy.proxy ParameterSetOperation]
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
      (let [elapsed (.getElapsedTime exec-info)
            queries (mapv logged-query query-infos)]
        (if (= (count queries) 1)
          (log/log logger :info ::sql/query {:query (first queries), :elapsed elapsed})
          (log/log logger :info ::sql/batch-query {:queries queries, :elapsed elapsed}))))))

(defn- wrap-logger [datasource logger]
  (doto (ProxyDataSource. datasource)
    (.addListener (logging-listener logger))))

(defn- unwrap-logger [^DataSource datasource]
  (.unwrap datasource DataSource))

(defmethod ig/init-key :duct.database.sql/hikaricp
  [_ {:keys [logger connection-uri jdbc-url] :as options}]
  (when logger (log/log logger :report ::initializing))
  (let [url (or jdbc-url connection-uri)
        ds  (-> (dissoc options :logger)
                (assoc :jdbc-url url)
                (hikari-cp/make-datasource)
                (cond-> logger (wrap-logger logger)))]
    (sql/->Boundary {:logger logger :datasource ds})))

(defmethod ig/halt-key! :duct.database.sql/hikaricp [_ {:keys [spec logger]}]
  (let [ds (unwrap-logger (:datasource spec))]
    (when logger (log/log logger :report ::halting))
    (hikari-cp/close-datasource ds)))
