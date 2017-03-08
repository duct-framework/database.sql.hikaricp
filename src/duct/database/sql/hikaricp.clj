(ns duct.database.sql.hikaricp
  (:require [integrant.core :as ig]
            [duct.database.sql :as sql]
            [hikaricp.core :as hikaricp]))

(defmethod ig/init-key :duct.database.sql/hikaricp [_ options]
  (sql/->Boundary {:datasource (hikaricp/make-datasource options)}))

(defmethod ig/halt-key! :duct.database.sql/hikaricp [_ {:keys [spec]}]
  (hikaricp/close-datasource (:datasource spec)))
