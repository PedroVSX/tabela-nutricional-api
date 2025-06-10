(ns tabela-nutricional-api.db
  (:import [java.time LocalDate]))

;; Contadores para IDs
(def contador-usuarios (atom 0))

;; Simulação de banco de dados com atoms
(def usuarios (atom {}))                ;; ID => usuário
(def atividades-registradas (atom []))  ;;Lista de alimentos registrados
(def alimentos-consumidos (atom []))    ;; Lista de alimentos registrados

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

(defn registrar-atividade
  [atividade tempo calorias data]
  (let [registro {:atividade atividade
                  :tempo tempo
                  :calorias calorias
                  :data data}]
    (swap! atividades-registradas conj registro)
    registro))

(defn listar-atividades []
  @atividades-registradas)

;; ================================
;; ALIMENTOS
;; ================================


(defn listar-alimentos-consumidos []
  @alimentos-consumidos)

(defn registrar-alimento-consumido
  [alimento caloria quantidade data]
  (let [registro {:alimento alimento
                  :caloria caloria
                  :quantidade quantidade
                  :data-consumo data}]
    (swap! alimentos-consumidos conj registro)
    registro))

