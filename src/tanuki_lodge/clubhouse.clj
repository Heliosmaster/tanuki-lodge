(ns tanuki-lodge.clubhouse
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [mount.core :as mount :refer [defstate]]
            [camel-snake-kebab.core :refer :all]))

(defonce config (atom nil))

(defn clubhouse-url [url] (str url "?token=" (:token (mount/args))))


(defn get-project []
  (first (:body (client/get (clubhouse-url "https://api.clubhouse.io/api/v2/projects")
                            {:as :json-kebab-keys}))))


(defn get-workflows []
  (first (:body (client/get (clubhouse-url "https://api.clubhouse.io/api/v2/workflows")
                            {:as :json-kebab-keys}))))

(defn workflow-states []
  (let [workflows-response (get-workflows)]
    (into {} (map (fn [state] [(->kebab-case-keyword (:name state)) (:id state)]) (:states workflows-response)))))


(defn move-stories-to-workflow [story-ids workflow-key]
  (client/put (clubhouse-url "https://api.clubhouse.io/api/v2/stories/bulk")
              {:headers {"Content-Type" "application/json"}
               :body (json/encode {:workflow-state-id (get-in @config [:states workflow-key])
                                   :story-ids story-ids}
                                  {:key-fn ->snake_case_string})}))

(defn create-story [^String story-name]
  (client/post (clubhouse-url "https://api.clubhouse.io/api/v2/stories")
               {:headers {"Content-Type" "application/json"}
                :body (json/encode {:name story-name
                                    :project-id 573} ;; TODO
                                   {:key-fn ->snake_case_string})}))

(defn delete-story [story-id]
  (client/delete (clubhouse-url (str "https://api.clubhouse.io/api/v2/stories/" story-id))))


(defn initialize! []
  (reset! config {:states (workflow-states)}))

(defstate
  clubhouse-connection
  :start (initialize!))