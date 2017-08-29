(ns duct.database.sql.hikaricp
  (:require [integrant.core :as ig]
            [duct.database.sql :as sql]
            [duct.logger :as log]
            [hikari-cp.core :as hikari-cp])
  (:import [net.ttddyy.dsproxy.support ProxyDataSource]
           [net.ttddyy.dsproxy.listener QueryExecutionListener]))

(defn- logging-listener [logger]
  (reify QueryExecutionListener
    (beforeQuery [_ _ _])
    (afterQuery [_ exec-info query-infos]
      (let [elapsed (.getElapsedTime exec-info)
            queries (mapv #(.getQuery %) query-infos)]
        (if (= (count queries) 1)
          (log/log logger :info ::query {:query (first queries), :elapsed elapsed})
          (log/log logger :info ::batch-query {:queries queries, :elapsed elapsed}))))))

(defn- wrap-logger [datasource logger]
  (doto (ProxyDataSource. datasource)
    (.addListener (logging-listener logger))))

(defmethod ig/init-key :duct.database.sql/hikaricp [_ {:keys [logger] :as options}]
  (sql/->Boundary {:datasource (-> (hikari-cp/make-datasource options)
                                   (cond-> logger (wrap-logger logger)))}))

(defmethod ig/halt-key! :duct.database.sql/hikaricp [_ {:keys [spec]}]
  (hikari-cp/close-datasource (:datasource spec)))
