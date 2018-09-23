(ns tanuki-lodge.core
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE routes]]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [ring.middleware.json :refer [wrap-json-body]]
            [clojure.tools.logging :as log]
            [tanuki-lodge.clubhouse :as clubhouse]
            [mount.core :as mount :refer [defstate]])
  (:gen-class))

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
                (clubhouse/move-stories-to-workflow story-ids :in-progress)
                (clubhouse/move-stories-to-workflow story-ids :review))
      :merged (clubhouse/move-stories-to-workflow story-ids :to-be-deployed)
      (log/error "Could not process merge request: " {:status status :wip? wip? :story-ids story-ids :description description}))))

(defroutes handler
  (POST "/" {:keys [headers body]}
    (let [parsed-body (transform-keys ->kebab-case-keyword body)]
      (do (case (get headers "x-gitlab-event")
            "Push Hook" (handle-push parsed-body)
            "Merge Request Hook" (handle-merge-request parsed-body))
          {:status 200
           :body "OK"})))
  (route/not-found "Not found")
  )

(def app
  (-> (routes handler)
      (wrap-json-body {:keywords? true})))

(defstate server
          :start (do (jetty/run-jetty #'app {:join? false :port (Integer/parseInt (env :port "9090"))})
                     (log/info "Server started"))
          :stop (do (log/info "Stopping server")
                    (.stop server)))

(defn go []
  (mount/start))

(defn -main []
  (go))