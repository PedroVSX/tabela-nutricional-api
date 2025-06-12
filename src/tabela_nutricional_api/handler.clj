(ns tabela-nutricional-api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as str]
            [cheshire.core :as json]
            [cheshire.generate :refer [add-encoder]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :as response]
            [tabela-nutricional-api.db :as db]
            [tabela-nutricional-api.user :as user]
            [tabela-nutricional-api.nutrition :as nutrition]
            [tabela-nutricional-api.exercise :as exercise]
            [tabela-nutricional-api.activity :as activity]
            [tabela-nutricional-api.date :as date])

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

(defn extrato-alimento [request]
  (let [{:keys [data-inicial data-final] :as body} (:body request)]
    (println "Body já convertido (mapeado por wrap-json-body):" body)
    (println "Datas recebidas:" data-inicial data-final)

    (let [alimentos (db/listar-alimentos-consumidos)]
      (println "Alimentos cadastrados:" alimentos)

      (let [filtrados
            (filter (fn [a]
                      (try
                        (and (string? (:data-consumo a))
                             (date/data-no-periodo?
                               (:data-consumo a)
                               data-inicial
                               data-final))
                        (catch Exception e
                          (println "Erro no registro:" a "->" (.getMessage e))
                          false)))
                    alimentos)

            ordenados (sort-by #(date/data-int (:data-consumo %)) filtrados)]
        (println "Alimentos filtrados e ordenados no período:" ordenados)
        (como-json ordenados)))))

(defn extrato-atividade [request]
  (let [{:keys [data-inicial data-final] :as body} (:body request)]
    (println "Body já convertido (mapeado por wrap-json-body):" body)
    (println "Datas recebidas:" data-inicial data-final)

    ;; Extrai as atividades como coleção a partir do mapa
    (let [atividades (db/listar-atividades)]
      (println "Atividades cadastradas:" atividades)

      (let [filtrados
            (filter (fn [a]
                      (try
                        (and (string? (:data a))
                             (date/data-no-periodo?
                               (:data a)
                               data-inicial
                               data-final))
                        (catch Exception e
                          (println "Erro no registro:" a "->" (.getMessage e))
                          false)))
                    atividades)

            ordenados (sort-by #(date/data-int (:data %)) filtrados)]
        (println "Atividades filtrados e ordenados no período:" ordenados)
        (como-json ordenados)))))

;; Rotas
(defroutes app-routes

           ;; Mensagem inicial
           (GET "/" []
             (como-json {:mensagem "Bem-vindo a API de Tabela Nutricional!"}))

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
               (let [required-keys [:atividade :tempo :data :calorias]
                     missing-keys (remove #(contains? body %) required-keys)]
                 (if (empty? missing-keys)
                   (como-json (activity/cadastrar-atividade body) 201)
                   (como-json {:erro "Dados incompletos"
                               :campos-faltantes missing-keys} 400)))
               (catch Exception e
                 (como-json {:erro "Falha no cadastro"
                             :detalhes (.getMessage e)} 500))))



           ;(GET "/exercicios/:atividade/:tempo" [atividade tempo]
           ;  (try
           ;    (let [duration (try (Integer/parseInt tempo) (catch Exception _ nil))]
           ;      ;(println duration)
           ;      (if (nil? duration)
           ;        (como-json {:erro "O tempo precisa ser um número inteiro"} 400)
           ;        (let [res (exercise/calcular-gasto-calorico atividade duration)]
           ;          (if (map? res)
           ;            (como-json res)
           ;            (como-json {:resultados res})))))
           ;
           ;    (catch Exception e
           ;      (como-json {:erro "Erro ao buscar exercício"
           ;                  :detalhes (.getMessage e)} 500))))


           (GET "/exercicios/:atividade/:tempo/:peso" [atividade tempo peso]
             (try
               (let [duration (try (Integer/parseInt tempo) (catch Exception _ nil))
                     weight (try (Integer/parseInt peso) (catch Exception _ nil))]
                 (if (or (nil? duration) (nil? weight))
                   (como-json {:erro "O tempo e peso precisam ser números inteiros"} 400)
                   (let [res (exercise/calcular-gasto-calorico atividade duration weight)]
                     (if (or (map? res) (vector? res))
                       (como-json (if (map? res) res {:resultados res}))
                       (como-json {:erro "Resposta inesperada da API"} 500)))))
                 (catch Exception e
                   (como-json {:erro "Erro ao buscar exercício"
                               :detalhes (.getMessage e)} 500))))


             (POST "/consumo" {body :body}
             (try
               (let [{:keys [alimento caloria quantidade data]} body]
                 (if (or (str/blank? alimento) (nil? caloria) (nil? quantidade) (str/blank? data)) ;; str/blank? verifica se a string está vazia, nula ou só com espaços
                   (como-json {:erro "Campos obrigatórios: alimento, caloria, quantidade, data"} 400)
                   (let [registro (db/registrar-alimento-consumido alimento caloria quantidade data)]
                     (como-json {:mensagem "Alimento registrado com sucesso" :registro registro} 201))))
               (catch Exception e
                 (como-json {:erro "Erro ao registrar consumo" :detalhes (.getMessage e)} 500))))


           (GET "/consumo" []
             (como-json (db/listar-alimentos-consumidos)))


           ;; GET para consultar todos os alimentos consumidos
           (GET "/alimentos" []
             {:status 200
              :body @db/alimentos-consumidos})

           (POST "/extrato/alimento" [] extrato-alimento)

           (POST "/extrato/atividade" [] extrato-atividade)


           (GET "/favicon.ico" []
             {:status 204
              :headers {}
              :body ""}))


;; Aplicação com middlewares
(def app
  (-> app-routes
      (wrap-json-response)
      (wrap-json-body {:keywords? true :bigdecimals? true})
      (wrap-defaults api-defaults)))
