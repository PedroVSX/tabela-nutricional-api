(ns tabela-nutricional-api.nutrition
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [environ.core :refer [env]]))

(def api-key "CfxIp/1Y9YYK8TSVt695Zw==wZ3jWxSPX8zn5e4L")
(def api-base-url "https://api.api-ninjas.com/v1/nutrition")

(defn buscar-alimentos [query]
  (let [response (http/get api-base-url
                           {
                            :headers {"X-Api-Key" api-key}
                            :query-params {:query query}
                            :accept :json
                            :throw-exceptions false
                            })]
    (cond
      (= 200 (:status response))
      {:sucesso true
       :opcoes (json/parse-string (:body response) true)}

      (= 401 (:status response))
      {:sucesso false
       :erro "Chave da API inválida ou não fornecida"}

      :else
      {:sucesso false
       :erro (str "Erro na API externa - Status: " (:status response))}
      )
    )
  )

(defn obter-dados-nutricionais [alimento-selecionado] alimento-selecionado)


