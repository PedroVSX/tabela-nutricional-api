(ns tabela-nutricional-api.nutrition
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [tabela-nutricional-api.db :as db]
            [tabela-nutricional-api.translate :as translate]
            [environ.core :refer [env]]
            [cheshire.core :as json]))

(def api-key "kleyAUZFXaEpQhYdRFxPW8gTjrxcggJZZ114yX3a")
(def base-url "https://api.nal.usda.gov/fdc/v1/foods/search")

(defn buscar-alimentos [query]
  (let [params {"api_key" api-key
                "query" query
                "dataType" ["Survey (FNDDS)"]
                "pageSize" 10}
        response (http/get base-url
                           {:query-params params
                            :accept :json
                            :throw-exceptions false})]

    (cond
      (= 200 (:status response))
      (let [body (json/parse-string (:body response) true)]
        {:sucesso true
         :opcoes (:foods body)})

      (= 401 (:status response))
      {:sucesso false
       :erro "Chave da API inválida ou não fornecida"}

      :else
      {:sucesso false
       :erro (str "Erro na API externa - Status: " (:status response)
                  " - Body: " (:body response))})))

(defn extrair-info-alimento [alimento]
  (let [desc (:description alimento)
        marca (:brandName alimento)
        nome-completo (if marca
                        (str desc " - " marca)
                        desc)
        nome-traduzido (translate/traduzir nome-completo "en|pt")
        porcao (when (and (:servingSize alimento) (:servingSizeUnit alimento))
                 (str (:servingSize alimento) " " (:servingSizeUnit alimento)))
        calorias (some #(when (= "Energy" (:nutrientName %)) ;; some retorna o primeiro valor correspondente. // when -> é como um if, mas sem o else.
                          (:value %))
                       (:foodNutrients alimento))]
    (when nome-completo
      {:nome (str/trim nome-traduzido)
       :porcao porcao
       :calorias calorias})))

(defn alimentos-info [query]
  (let [query-traduzido (translate/traduzir query "pt|en")
        resultado (buscar-alimentos query-traduzido)]
    (if (:sucesso resultado)
      (->> (:opcoes resultado)
           (map extrair-info-alimento)
           (filter some?))
      (do (println "Erro:" (:erro resultado))
          nil))))



