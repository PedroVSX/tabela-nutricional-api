(ns tabela-nutricional-api.user
  (:require [tabela-nutricional-api.db :as db]))

(defn cadastrar-usuario [dados]
  (db/cadastrar-usuario dados))

(defn obter-usuario [id]
  (if-let [usuario (db/buscar-usuario id)]
    usuario
    (throw (ex-info "Usuário não encontrado" {:id id :status 404}))))
