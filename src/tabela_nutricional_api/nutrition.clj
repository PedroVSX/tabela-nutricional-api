(ns tabela-nutricional-api.nutrition
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [cheshire.core :as json]))

(def api-key "kleyAUZFXaEpQhYdRFxPW8gTjrxcggJZZ114yX3a")
(def base-url "https://api.nal.usda.gov/fdc/v1/foods/search")

(defn buscar-alimentos [query]
  (let [response (http/get base-url
                           {:query-params {"api_key" api-key
                                           "query" query}
                            :accept :json
                            :throw-exceptions false})]
    (cond
      (= 200 (:status response))
      (let [body (json/parse-string (:body response) true)]
        {:sucesso true
         :opcoes (:foods body)}) ;; Extrai a lista "foods"

      (= 401 (:status response))
      {:sucesso false
       :erro "Chave da API inválida ou não fornecida"}

      :else
      {:sucesso false
       :erro (str "Erro na API externa - Status: " (:status response))})))


;;POSSUI O NOME DA FABRICANTE
(defn extrair-info-alimento [alimento]
  (let [desc (:description alimento)
        marca (:brandName alimento)
        nome-completo (if marca
                        (str desc " - " marca)
                        desc)
        porcao (when (and (:servingSize alimento) (:servingSizeUnit alimento))
                 (str (:servingSize alimento) " " (:servingSizeUnit alimento)))
        calorias (some #(when (= "Energy" (:nutrientName %)) (:value %))
                       (:foodNutrients alimento))]
    (when (and nome-completo porcao calorias)
      {:nome (str/trim nome-completo)
       :porcao porcao
       :calorias calorias})))

(defn alimentos-info [query]
  (let [resultado (buscar-alimentos query)]
    (if (:sucesso resultado)
      (->> (:opcoes resultado)
           (map extrair-info-alimento)
           (filter some?)) ;; remove os nils (alimentos ignorados)
      (do (println "Erro:" (:erro resultado))
          nil))))
