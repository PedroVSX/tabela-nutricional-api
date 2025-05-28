(ns tabela-nutricional-api.user
  (:require [tabela-nutricional-api.db :as db]
            [clojure.string :as str]))

(defn- parse-number [n]
  (cond
    (string? n) (try (Double/parseDouble (str/trim n)) (catch Exception _ 0.0))
    (number? n) (double n)
    :else 0.0))

(defn calcular-tmb [{:keys [sexo peso altura idade]}]
  (let [peso-num (parse-number peso)
        altura-num (parse-number altura)
        idade-num (parse-number idade)]
    (if (= (str/lower-case sexo) "masculino")
      (+ 66.5 (* 13.75 peso-num) (* 5.003 altura-num) (- (* 6.755 idade-num)))
      (+ 655.1 (* 9.563 peso-num) (* 1.850 altura-num) (- (* 4.676 idade-num))))))

(defn cadastrar-usuario [dados]
  (let [usuario (db/cadastrar-usuario dados)
        tmb (calcular-tmb dados)]
    (assoc usuario :tmb tmb)))

(defn obter-usuario [id]
  (if-let [usuario (db/buscar-usuario id)]
    (assoc usuario :tmb (calcular-tmb usuario))
    (throw (ex-info "Usuário não encontrado" {:id id :status 404}))))