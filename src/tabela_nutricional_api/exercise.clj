(ns tabela-nutricional-api.exercise
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))

(def api-key "tFFftSLq2cuyKWH07bxoQg==y31S5MYKZvEG7Ohi")
(def base-url "https://api.api-ninjas.com/v1/caloriesburned")

;(defn calcular-gasto-calorico [atividade duration]
;  (println atividade duration)
;  (let [response (http/get base-url
;                           {:headers {"X-Api-Key" api-key}
;                            :query-params {"activity" atividade
;                                           "duration" duration
;                                           "weight" peso}
;                            :accept :json
;                            :throw-exceptions false})]
;    (cond
;      (= 200 (:status response))
;
;      (let [body (json/parse-string (:body response) true)]
;        (println response)
;        (println body)
;        (if (seq body)
;          ;; Retorna todos os resultados mapeados no formato desejado
;          (map (fn [res]
;                 {:atividade (:name res)
;                  :tempo duration
;                  :calorias (:total_calories res)})
;               body)
;          {:erro "Nenhum resultado retornado pela API"}))
;
;      (= 401 (:status response))
;      {:erro "Chave da API inválida"}
;
;      :else
;      {:erro (str "Erro na API externa - Status: " (:status response))})))
;
(defn calcular-gasto-calorico
  ([atividade tempo]
   (calcular-gasto-calorico atividade tempo nil)) ;; peso opcional
  ([atividade tempo peso]
   (println "Atividade:" atividade "Tempo:" tempo "Peso:" peso)
   (let [query-params (cond-> {"activity" atividade
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
           (map (fn [res]
                  {:atividade (:name res)
                   :tempo (:duration_minutes res)
                   :calorias (:total_calories res)})
                body)
           {:erro "Nenhum resultado retornado pela API"}))

       (= 401 (:status response))
       {:erro "Chave da API inválida"}

       :else
       {:erro (str "Erro na API externa - Status: " (:status response))}))))
