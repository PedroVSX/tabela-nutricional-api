(ns tabela-nutricional-api.date
  (:require [clojure.string :as str]))

(defn inverter-data [data-str]
  (let [[d m a] (str/split data-str #"/")
        d (format "%02d" (Integer/parseInt d))
        m (format "%02d" (Integer/parseInt m))]
    (str a "/" m "/" d)))


(defn filtra-por-periodo [lista-alimentos data-inicial data-final]
  (let [data-inv-inicial (inverter-data data-inicial)
        data-inv-final   (inverter-data data-final)]
    (filter (fn [item]
              (let [data (inverter-data (:data-consumo item))]
                (and (>= data data-inv-inicial)
                     (<= data data-inv-final))))
            lista-alimentos)))
