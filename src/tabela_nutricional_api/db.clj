(ns tabela-nutricional-api.db
  (:import [java.time LocalDate]))

(def contador-usuarios (atom 0))
(def usuarios (atom {}))

(def contador-alimentos (atom 0))
(def alimentos (atom []))

(def atividades (atom []))

;; USUÁRIO
(defn buscar-usuario [id]
  (get @usuarios id))

(defn proximo-id-usuario []
  (swap! contador-usuarios inc))

(defn cadastrar-usuario [dados]
  (let [id (proximo-id-usuario)
        usuario (assoc dados :id id :data-cadastro (LocalDate/now))]
    (swap! usuarios assoc id usuario)
    usuario
    )
  )

;; ALIMENTOS
(defn proximo-id-alimento []
  (swap! contador-alimentos inc))

(defn calcular-calorias [calorias-por-porcao quantidade serving-size]
  (let [ss (if (and serving-size (pos? serving-size))
             serving-size
             100)]
    (* calorias-por-porcao (/ quantidade ss))))

(defn registrar-alimento [usuario-id nome quantidade data dados-nutricionais]
  (let [id (proximo-id-alimento)
        calorias-por-porcao (:calories dados-nutricionais)
        serving-size (:serving_size_g dados-nutricionais)
        calorias (calcular-calorias calorias-por-porcao quantidade serving-size)
        registro {:id id
                  :usuario-id usuario-id
                  :alimento nome
                  :quantidade quantidade
                  :data data
                  :calorias calorias
                  :dados-nutricionais dados-nutricionais}]
    (swap! alimentos conj registro)
    registro
    )
  )

(defn registrar-alimento-completo [usuario-id alimento-selecionado quantidade data]
  (registrar-alimento usuario-id
                      (:name alimento-selecionado)
                      quantidade
                      data
                      (select-keys alimento-selecionado [:calories :serving_size_g])
                      )
  )