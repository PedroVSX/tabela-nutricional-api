(ns tabela-nutricional-api.date
  (:require [clojure.string :as str]))

(defn data-int [data]
  ;; "dd/MM/yyyy" -> yyyyMMdd como número
  (Integer/parseInt
    (apply str [(subs data 6 10)
                (subs data 3 5)
                (subs data 0 2)])))

(defn data-no-periodo? [data-consumo data-inicial data-final]
  (let [dc (data-int data-consumo)
        di (data-int data-inicial)
        df (data-int data-final)]
    (and (>= dc di) (<= dc df))))

