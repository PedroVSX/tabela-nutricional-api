(ns tabela-nutricional-api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as str]
            [cheshire.core :as json]
            [cheshire.generate :refer [add-encoder]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [tabela-nutricional-api.db :as db]
            [tabela-nutricional-api.user :as user]
            [tabela-nutricional-api.nutrition :as nutrition]
            [tabela-nutricional-api.exercise :as exercise]
            [tabela-nutricional-api.activity :as activity]) ; Corrigido: namespace correto
  (:import (java.time LocalDate)))

;; Encoder para datas no formato JSON
(add-encoder LocalDate
             (fn [date jsonGenerator]
               (.writeString jsonGenerator (str date))))

;; Resposta padrão JSON
(defn como-json [conteudo & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/generate-string conteudo)})

;; Rotas
(defroutes app-routes

           ;; Mensagem inicial
           (GET "/" []
             (como-json {:mensagem "Bem-vindo à API de Tabela Nutricional!"}))

           ;; Cadastro de usuário
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

           ;; Busca usuário por ID
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

           ;; Busca alimentos da API externa por nome
           (GET "/alimentos/:query" [query]
             (try
               (let [res (nutrition/alimentos-info query)]
                 (if (not (empty? res))
                   (como-json res)
                   (como-json {:erro "Nenhum alimento encontrado"} 404)))
               (catch Exception e
                 (como-json {:erro "Erro ao buscar alimentos"
                             :detalhes (.getMessage e)} 500))))

           ;; Cadastro de atividade
           (POST "/atividade" {body :body}
             (try
               (let [required-keys [:atividade :tempo]
                     missing-keys (remove #(contains? body %) required-keys)]
                 (if (empty? missing-keys)
                   (como-json (activity/cadastrar-atividade body) 201)
                   (como-json {:erro "Dados incompletos"
                               :campos-faltantes missing-keys} 400)))
               (catch Exception e
                 (como-json {:erro "Falha no cadastro"
                             :detalhes (.getMessage e)} 500))))

           (GET "/atividade" []
             (como-json (vals @db/atividades)))


           ;; Busca atividade por ID
           (GET "/atividade/:id" [id]
             (try
               (let [id-num (try (Integer/parseInt id) (catch Exception _ nil))
                     atividade (activity/obter-atividade id-num)]
                 (if atividade
                   (como-json atividade)
                   (como-json {:erro "Atividade não encontrada"} 404)))
               (catch Exception e
                 (como-json {:erro "Erro na busca"
                             :detalhes (.getMessage e)} 500))))

           ;; Busca de exercícios com cálculo calórico
           (GET "/exercicios/:atividade/:tempo" [atividade tempo]
             (try
               (let [duration (try (Integer/parseInt tempo) (catch Exception _ nil))]
                 (if (nil? duration)
                   (como-json {:erro "O tempo precisa ser um número inteiro"} 400)
                   (let [res (exercise/calcular-gasto-calorico atividade duration)]
                     (if (map? res)
                       (como-json res)
                       (como-json {:resultados res})))))
               (catch Exception e
                 (como-json {:erro "Erro ao buscar exercício"
                             :detalhes (.getMessage e)} 500))))

           ;(POST "/consumo" {body :body}
           ;  ;(println "Recebido no backend:" body)
           ;  (try
           ;    (let [{:keys [alimento caloria quantidade]} body]
           ;      ;; validações simples
           ;      (if (or (str/blank? alimento) (nil? caloria) (nil? quantidade))
           ;        (como-json {:erro "Campos obrigatórios: alimento, caloria, quantidade"} 400)
           ;        (let [registro (db/registrar-alimento-consumido alimento caloria quantidade)]
           ;          (como-json {:mensagem "Alimento registrado com sucesso" :registro registro} 201))))
           ;    (catch Exception e
           ;      (como-json {:erro "Erro ao registrar consumo" :detalhes (.getMessage e)} 500))))

           (POST "/consumo" {body :body}
             (try
               (let [{:keys [alimento caloria quantidade data]} body]
                 (if (or (str/blank? alimento) (nil? caloria) (nil? quantidade) (str/blank? data))
                   (como-json {:erro "Campos obrigatórios: alimento, caloria, quantidade, data"} 400)
                   (let [registro (db/registrar-alimento-consumido alimento caloria quantidade data)]
                     (como-json {:mensagem "Alimento registrado com sucesso" :registro registro} 201))))
               (catch Exception e
                 (como-json {:erro "Erro ao registrar consumo" :detalhes (.getMessage e)} 500))))


           (GET "/consumo" []
             (como-json (db/listar-alimentos-consumidos)))


           (GET "/consumo" []
             (como-json (db/listar-alimentos-consumidos)))


           (GET "/api/alimentos" [busca]
             (if (or (nil? busca) (str/blank? busca))
               (como-json {:erro "Parâmetro 'busca' é obrigatório"} 400)
               (try
                 (let [resultado (nutrition/alimentos-info busca)]
                   (if (seq resultado)
                     (como-json resultado)
                     (como-json {:erro "Nenhum alimento encontrado"} 404)))
                 (catch Exception e
                   (como-json {:erro "Erro ao buscar alimentos"
                               :detalhes (.getMessage e)} 500)))))


           )


;; Aplicação com middlewares
(def app
  (-> app-routes
      (wrap-json-response)
      (wrap-json-body {:keywords? true :bigdecimals? true})
      (wrap-defaults api-defaults)))
