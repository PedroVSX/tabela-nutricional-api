(ns tabela-nutricional-api.exercise
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [tabela-nutricional-api.translate :as translate]))

(def api-key "tFFftSLq2cuyKWH07bxoQg==y31S5MYKZvEG7Ohi")
(def base-url "https://api.api-ninjas.com/v1/caloriesburned")

(defn calcular-gasto-calorico
  ([atividade tempo peso]
   (println "Atividade:" atividade "Tempo:" tempo "Peso:" peso)
   (let [atividade-en (translate/traduzir atividade "pt|en")

         query-params (cond-> {"activity" atividade-en
                               "duration" tempo}
                              peso (assoc "weight" peso))
         response (http/get base-url
                            {:headers {"X-Api-Key" api-key}
                             :query-params query-params
                             :accept :json
                             :throw-exceptions false})]
     (cond
       (= 200 (:status response))
       (let [body (json/parse-string (:body response) true)]
         (println "Resposta bruta:" response)
         (println "Body parseado:" body)
         (if (seq body)
           ;; Retorna todos os resultados mapeados no formato desejado
           (mapv (fn [res]
                  {:atividade (translate/traduzir (:name res) "en|pt")
                   :tempo (:duration_minutes res)
                   :calorias (:total_calories res)})
                body)
           {:erro "Nenhum resultado retornado pela API"}))

       (= 401 (:status response))
       {:erro "Chave da API inválida"}

       :else
       {:erro (str "Erro na API externa - Status: " (:status response))}))))
