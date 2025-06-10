(ns tabela-nutricional-api.activity
  (:require [tabela-nutricional-api.db :as db]))

(defn cadastrar-atividade [{:keys [atividade tempo calorias data]}]
  (db/registrar-atividade atividade tempo calorias data))
