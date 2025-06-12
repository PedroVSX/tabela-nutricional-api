(ns tabela-nutricional-api.translate
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))

(def base-url "https://api.mymemory.translated.net/get")

(defn traduzir [texto lang-pair]
  (try
    (let [response (http/get base-url
                             {:query-params {"q" texto
                                             "langpair" lang-pair}
                              :accept :json
                              :throw-exceptions false})
          body (json/parse-string (:body response) true)
          translated (get-in body [:responseData :translatedText])]
      (if translated
        translated
        (throw (Exception. (str "Erro na resposta da API: " body)))))
    (catch Exception e
      (println "Erro ao traduzir:" (.getMessage e))
      texto))) ;; fallback: retorna o texto original
