(ns tanuki-lodge.core
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE routes]]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [ring.middleware.json :refer [wrap-json-body]]
            [clojure.tools.logging :as log]
            [tanuki-lodge.clubhouse :as clubhouse]
            [mount.core :as mount :refer [defstate]]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:gen-class)
  (:import [java.io PushbackReader]))

(defmethod clj-http.client/coerce-response-body :json-kebab-keys [req resp]
  (clj-http.client/coerce-json-body req resp (memoize ->kebab-case-keyword) false))

(defn handle-push [body]
  #_(prn {:type :push :commits (map :id (:commits body))}))

(defn parse-story-ids [description]
  (->> description
       (re-seq #"\[ch\s?[0-9]+\]")
       (map #(Integer/parseInt (re-find #"[0-9]+" %)))))

(defn handle-merge-request [body]
  (let [description (get-in body [:object-attributes :description])
        story-ids (parse-story-ids description)
        wip? (get-in body [:object-attributes :work-in-progress])
        status (keyword (get-in body [:object-attributes :state]))]
    (case status
      :opened (if wip?
                (clubhouse/move-stories-to-workflow story-ids (->kebab-case-keyword (:merge-request-open-wip (mount/args))))
                (clubhouse/move-stories-to-workflow story-ids (->kebab-case-keyword (:merge-request-open (mount/args)))))
      :merged (clubhouse/move-stories-to-workflow story-ids (->kebab-case-keyword (:merge-request-merged (mount/args))))
      (log/error "Could not process merge request: " {:status status :wip? wip? :story-ids story-ids :description description}))))

(defroutes handler
  (POST "/" {:keys [headers body]}
    (let [parsed-body (transform-keys ->kebab-case-keyword body)]
      (do (case (get headers "x-gitlab-event")
            "Push Hook" (handle-push parsed-body)
            "Merge Request Hook" (handle-merge-request parsed-body))
          {:status 200
           :body "OK"})))
  (route/not-found "Not found"))

(def app
  (-> (routes handler)
      (wrap-json-body {:keywords? true})))

(defstate
  server
  :start (do (jetty/run-jetty #'app {:join? false :port (Integer/parseInt (or (:server-port (mount/args)) "9090"))}))
  :stop (do (log/info "Stopping server")
            (.stop server)))

(defn go
  ([]
   (go "config.edn"))
  ([config-file]
   (let [config (edn/read-string (slurp (io/file config-file)))]
     (prn "Starting application with configuration: " config)
     (mount/start-with-args config))))

(defn -main [& [config-file]]
  (go config-file))