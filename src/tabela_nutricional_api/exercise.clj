(ns tabela-nutricional-api.exercise
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))

(def api-key "tFFftSLq2cuyKWH07bxoQg==y31S5MYKZvEG7Ohi")
(def base-url "https://api.api-ninjas.com/v1/caloriesburned")

(defn calcular-gasto-calorico [atividade duration]
  (let [response (http/get base-url
                           {:headers {"X-Api-Key" api-key}
                            :query-params {"activity" atividade
                                           "duration_minutes" duration}
                            :accept :json
                            :throw-exceptions false})]
    (cond
      (= 200 (:status response))
      (let [body (json/parse-string (:body response) true)]
        (if (seq body)
          ;; Retorna todos os resultados mapeados no formato desejado
          (map (fn [res]
                 {:atividade (:name res)
                  :tempo duration
                  :calorias (:total_calories res)})
               body)
          {:erro "Nenhum resultado retornado pela API"}))

      (= 401 (:status response))
      {:erro "Chave da API inválida"}

      :else
      {:erro (str "Erro na API externa - Status: " (:status response))})))