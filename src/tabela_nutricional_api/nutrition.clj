(ns tabela-nutricional-api.nutrition
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [environ.core :refer [env]]))

(def api-key "AlaGDaMHBtpOOLh8eXc3tbTAaZsGY6Q1Eye5OoGX")
(def api-base-url "https://api.nal.usda.gov/fdc/v1")

(defn buscar-alimentos [query]
  (try
    (let [response (http/get (str api-base-url "/foods/search")
                             {:query-params {:api_key api-key
                                             :query query
                                             :pageSize 10
                                             :dataType ["Survey (FNDDS)"]}
                              :accept :json
                              :throw-exceptions false})]
      (if (= 200 (:status response))
        (let [body (json/parse-string (:body response) true)
              alimentos (map (fn [item]
                               {:fdcId (:fdcId item)
                                :name (:description item)
                                :dataType (:dataType item)
                                :nutrients (:foodNutrients item)})
                             (:foods body))]
          {:sucesso true
           :opcoes alimentos})
        {:sucesso false
         :erro (str "Erro na API USDA: " (:status response))}
        )
      )
    (catch Exception e
      {:sucesso false
       :erro (str "Erro de conexão: " (.getMessage e))}
      )
    )
  )

(defn extrair-nutriente [nutrientes nome]
  (some #(when (= (:nutrientName %) nome) %) nutrientes))

(defn obter-dados-nutricionais [alimento]
  (let [nutrientes (:nutrients alimento)]
    {:calorias     (:value (extrair-nutriente nutrientes "Energy"))
     :proteinas    (:value (extrair-nutriente nutrientes "Protein"))
     :carboidratos (:value (extrair-nutriente nutrientes "Carbohydrate, by difference"))
     :gorduras     (:value (extrair-nutriente nutrientes "Total lipid (fat)"))}
    )
  )



