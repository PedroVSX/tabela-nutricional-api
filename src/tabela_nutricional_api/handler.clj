(ns tabela-nutricional-api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [cheshire.generate :refer [add-encoder]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [tabela-nutricional-api.db :as db]
            [tabela-nutricional-api.user :as user]
            [tabela-nutricional-api.nutrition :as nutrition]
            [tabela-nutricional-api.exercise :as exercise]
            )
  (:import (java.time LocalDate)))

;; Registra encoder para LocalDate
(add-encoder LocalDate
             (fn [date jsonGenerator]
               (.writeString jsonGenerator (str date))))

(defn como-json [conteudo & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/generate-string conteudo)})

(defroutes app-routes
           ;;INICIO
           (GET "/" []
             (como-json {:mensagem "Bem-vindo à API de Tabela Nutricional!"}))

           ;; PARA SALVAR OS DADOS DOS USUÁRIOS = http://localhost:3000/usuarios
           (POST "/usuarios" {body :body}
             (try
               (let [required-keys [:nome :altura :peso :idade :sexo]
                     missing-keys (remove #(contains? body %) required-keys)]
                 (if (empty? missing-keys)
                   (como-json (user/cadastrar-usuario body) 201)
                   (como-json {:erro "Dados incompletos"
                               :campos-faltantes missing-keys} 400)))
               (catch Exception e
                 (como-json {:erro "Falha no cadastro"
                             :detalhes (.getMessage e)} 500))))

           ;;BUSCA AS INFORMAÇÕES DO USUARIO A PARTIR DO SEU ID = http://localhost:3000/usuarios/1
           (GET "/usuarios/:id" [id]
             (try
               (let [id-num (try (Integer/parseInt id) (catch Exception _ nil))
                     usuario (user/obter-usuario id-num)]
                 (if usuario
                   (como-json usuario)
                   (como-json {:erro "Usuário não encontrado"} 404)))
               (catch Exception e
                 (como-json {:erro "Erro na busca"
                             :detalhes (.getMessage e)} 500))))


           ;; Lista completa de alimentos formatados a partir da API USDA = http://localhost:3000/alimentos/banana
           (GET "/alimentos/:query" [query]
             (try
               (let [res (nutrition/alimentos-info query)]
                 (if (seq res)
                   (como-json res)
                   (como-json {:erro "Nenhum alimento encontrado"} 404)))
               (catch Exception e
                 (como-json {:erro "Erro ao buscar alimentos"
                             :detalhes (.getMessage e)} 500))))


           ;; Lista completa de exercicios formatados a partir da API ninja = http://localhost:3000/exercicios/running/30
           (GET "/exercicios/:atividade/:tempo" [atividade tempo]
             (try
               (let [duration (try (Integer/parseInt tempo)
                                   (catch Exception _
                                     nil))]
                 (if (nil? duration)
                   (como-json {:erro "O tempo precisa ser um número inteiro"} 400)
                   (let [res (exercise/calcular-gasto-calorico atividade duration)]
                     (if (map? res)
                       ;; erro, retorna direto
                       (como-json res)
                       ;; lista de resultados
                       (como-json {:resultados res} )))))
               (catch Exception e
                 (como-json {:erro "Erro ao buscar exercício"
                             :detalhes (.getMessage e)} 500))))


           )

(def app
  (-> app-routes
      (wrap-defaults api-defaults)
      (wrap-json-body {:keywords? true :bigdecimals? true})
      wrap-json-response))