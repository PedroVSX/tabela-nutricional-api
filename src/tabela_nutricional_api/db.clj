(ns tabela-nutricional-api.db
  (:import [java.time LocalDate]))

;; Contadores para IDs
(def contador-usuarios (atom 0))
(def contador-alimentos (atom 0))
(def contador-atividade (atom 0))

;; Simulação de banco de dados com atoms
(def usuarios (atom {}))              ;; ID => usuário
(def alimentos (atom []))             ;; vetor de alimentos
(def atividades (atom {}))            ;; ID => atividade
(def alimentos-consumidos (atom []))  ;; Lista de alimentos registrados


;; ================================
;; USUÁRIOS
;; ================================

(defn buscar-usuario [id]
  (get @usuarios id))

(defn proximo-id-usuario []
  (swap! contador-usuarios inc))

(defn cadastrar-usuario [dados]
  (let [id (proximo-id-usuario)
        usuario (assoc dados :id id :data-cadastro (LocalDate/now))]
    (swap! usuarios assoc id usuario)
    usuario))


;; ================================
;; ATIVIDADES
;; ================================

(defn buscar-atividade [id]
  (get @atividades id))

(defn proximo-id-atividade []
  (swap! contador-atividade inc))

(defn cadastrar-atividade [dados]
  (let [id (proximo-id-atividade)
        atividade (assoc dados :id id :data-cadastro (LocalDate/now))]
    (swap! atividades assoc id atividade)
    atividade))


;; ================================
;; ALIMENTOS (antigo e novo)
;; ================================

(defn proximo-id-alimento []
  (swap! contador-alimentos inc))

;; Versão antiga para testes


(defn proximo-id-consumo []
  (swap! contador-alimentos inc))

(defn registrar-alimento-consumido
  [ alimento quantidade caloria]
  ;; Insere no banco e retorna confirmação
  ;; exemplo simples, ajuste conforme seu schema
  (let [registro {:alimento alimento
                  :caloria caloria
                  :quantidade quantidade
                  :data-consumo (java.time.LocalDate/now)}]
    ;; Suponha que insira no banco aqui, e retorna o registro
    registro))
