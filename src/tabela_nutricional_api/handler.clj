(ns tabela-nutricional-api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [cheshire.generate :refer [add-encoder]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [tabela-nutricional-api.db :as db]
            [tabela-nutricional-api.user :as user]
            [tabela-nutricional-api.nutrition :as nutrition])
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
           ;; USUÁRIOS
           (POST "/usuarios" {body :body}
             (try
               (let [required-keys [:nome :sexo :peso :altura :idade]
                     missing-keys (remove #(contains? body %) required-keys)]
                 (if (empty? missing-keys)
                   (como-json (user/cadastrar-usuario body) 201)
                   (como-json {:erro "Dados incompletos"
                               :campos-faltantes missing-keys} 400)))
               (catch Exception e
                 (como-json {:erro "Falha no cadastro"
                             :detalhes (.getMessage e)} 500))))

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

           ;; BUSCAR VALORES NUTRICIONAIS DOS ALIMENTOS
           (GET "/buscar-alimentos/:query" [query]
             (try
               (let [resultado (nutrition/buscar-alimentos query)]
                 (if (:sucesso resultado)
                   (como-json (:opcoes resultado))
                   (como-json {:erro "Falha na busca"
                               :detalhes (:erro resultado)} 400)))
               (catch Exception e
                 (como-json {:erro "Erro no servidor"
                             :detalhes (.getMessage e)} 500))))

           ;; ALIMENTOS
           (POST "/alimentos" {body :body}
             (try
               (let [required-keys [:usuario-id :alimento-selecionado :quantidade :data]
                     missing-keys (remove #(contains? body %) required-keys)]
                 (if (empty? missing-keys)
                   (let [registro (db/registrar-alimento-completo
                                    (:usuario-id body)
                                    (:alimento-selecionado body)
                                    (:quantidade body)
                                    (:data body))]
                     (como-json registro 201))
                   (como-json {:erro "Dados incompletos"
                               :campos-faltantes missing-keys} 400)))
               (catch Exception e
                 (como-json {:erro "Falha no registro"
                             :detalhes (.getMessage e)} 500))))

           (route/not-found (como-json {:erro "Endpoint não encontrado"} 404)))

(def app
  (-> app-routes
      (wrap-defaults api-defaults)
      (wrap-json-body {:keywords? true :bigdecimals? true})
      wrap-json-response))