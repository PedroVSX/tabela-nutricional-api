(ns tabela-nutricional-api.db
  (:import [java.time LocalDate]))

;; Contadores para IDs
(def contador-usuarios (atom 0))
(def contador-atividade (atom 0))

;; Simulação de banco de dados com atoms
(def usuarios (atom {}))              ;; ID => usuário
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
        atividade (assoc dados :id id)]                     ;; assoc -> adiciona (ou atualiza) a atividade no mapa.
    (swap! atividades assoc id atividade)                   ;; swap! -> altera o valor do atom de forma imutável e segura.
    atividade))


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

