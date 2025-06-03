(ns tabela-nutricional-api.activity
  (:require [tabela-nutricional-api.db :as db]))

(defn cadastrar-atividade [dados]
  (db/cadastrar-atividade dados))

(defn obter-atividade [id]
  (if-let [atividade (db/buscar-atividade id)]
    atividade
    (throw (ex-info "Atividade não encontrada" {:id id :status 404}))))
