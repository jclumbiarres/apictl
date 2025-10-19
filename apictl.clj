#!/usr/bin/env bb

(require '[babashka.curl :as curl]
         '[cheshire.core :as json]
         '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str])

;; Obtener headers con autenticación si existe
(defn get-headers []
  (let [token (System/getenv "APICTL_TOKEN")
        base-headers {"Accept" "application/json"}]
    (if token
      (assoc base-headers "Authorization" (str "Bearer " token))
      base-headers)))

;; Hacer GET a la API
(defn fetch-api [url]
  (let [response (curl/get url {:headers (get-headers)})]
    (:body response)))

;; Hacer POST a la API
(defn post-api [url json-body]
  (let [headers (assoc (get-headers) "Content-Type" "application/json")
        response (curl/post url 
                           {:headers headers
                            :body json-body})]
    (:body response)))

;; Validar que la respuesta es JSON
(defn validate-json [json-str]
  (try
    (json/parse-string json-str)
    true
    (catch Exception _e
      false)))

;; Procesar con jq usando shell
(defn process-with-jq [json-str jq-filter]
  (let [result (sh "jq" jq-filter :in json-str)]
    (if (zero? (:exit result))
      (:out result)
      (do
        (println "Error ejecutando jq:" (:err result))
        (System/exit 1)))))

;; Parsear archivo
(defn parse-file [filepath]
  (let [content (slurp filepath)
        lines (str/split-lines content)
        first-line (first lines)]
    (cond
      ;; POST: primera línea empieza con POST
      (str/starts-with? (str/upper-case first-line) "POST ")
      (let [url (str/trim (subs first-line 5))
            json-body (str/trim (str/join "\n" (rest lines)))]
        {:method :post
         :url url
         :body json-body})
      
      ;; GET: primera línea es la URL
      :else
      {:method :get
       :url (str/trim first-line)})))

;; Main
(when (empty? *command-line-args*)
  (println "Uso: apictl.clj <archivo> [filtro-jq]")
  (println "\nFormato del archivo:")
  (println "  GET:")
  (println "    https://api.example.com/users")
  (println "\n  POST:")
  (println "    POST https://api.example.com/users")
  (println "    {\"name\":\"John\",\"email\":\"john@example.com\"}")
  (System/exit 1))

(let [filepath (first *command-line-args*)
      jq-filter (or (second *command-line-args*) ".")
      request (parse-file filepath)]
  
  (case (:method request)
    :get
    (let [response (fetch-api (:url request))]
      (if (validate-json response)
        (print (process-with-jq response jq-filter))
        (do
          (println "Error: La respuesta no es JSON válido")
          (System/exit 1))))
    
    :post
    (let [json-body (:body request)]
      (if (validate-json json-body)
        (let [response (post-api (:url request) json-body)]
          (if (validate-json response)
            (print (process-with-jq response jq-filter))
            (do
              (println "Error: La respuesta no es JSON válido")
              (System/exit 1))))
        (do
          (println "Error: El cuerpo del POST no es JSON válido")
          (System/exit 1))))))
