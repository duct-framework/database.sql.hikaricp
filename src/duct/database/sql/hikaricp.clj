(ns duct.database.sql.hikaricp
  (:require [integrant.core :as ig]
            [duct.database.sql :as sql]
            [hikari-cp.core :as hikari-cp]))

(derive :duct.database.sql/hikaricp :duct.database/sql)

(defmethod ig/init-key :duct.database.sql/hikaricp [_ options]
  (sql/->Boundary {:datasource (hikari-cp/make-datasource options)}))

(defmethod ig/halt-key! :duct.database.sql/hikaricp [_ {:keys [spec]}]
  (hikari-cp/close-datasource (:datasource spec)))
