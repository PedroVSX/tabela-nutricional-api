(ns tabela-nutricional-api.activity
  (:require [tabela-nutricional-api.db :as db]))

(defn cadastrar-atividade [dados]
  (db/cadastrar-atividade dados))
