(ns default-transformer-stripping-data-issue.handler
  (:require
   [clojure.spec.alpha :as s]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [spec-tools.core :as stc]))

(s/def :g/bt string?)
(s/def :g/sp string?)
(s/def :g/sn string?)
(s/def :g/cn string?)
(s/def :p/when string?)
(s/def :p/who string?)
(s/def :p/how string?)
(s/def :p/why string?)

(s/def ::variant-a (stc/spec
                    (s/merge
                     (s/keys :req [:g/sn
                                   :g/bt
                                   :g/sp]
                             :opt [:g/cn
                                   :p/when
                                   :p/who
                                   :p/how
                                   :p/why])
                     (s/map-of
                      #{:g/bt
                        :g/sn
                        :g/sp
                        :g/cn
                        :p/when
                        :p/who
                        :p/how
                        :p/why}
                      (comp not nil?)))))

(s/def ::variant-b (stc/spec
                    (stc/merge
                     (s/keys :req [:g/sp :g/cn]
                             :opt [:p/who
                                   :p/how
                                   :p/why
                                   :p/when])
                     (s/map-of #{:g/sp
                                 :g/cn
                                 :p/when
                                 :p/who
                                 :p/how
                                 :p/why}
                               (comp not nil?)))))

(s/def ::update (stc/spec (s/or ::variant-a ::variant-b)))

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Default-transformer-stripping-data-issue"
                    :description "Compojure Api example"}
             :tags [{:name "api", :description "some apis"}]}}}
    (routes
     (context "/issue" []
       :coercion :spec
       (resource
        {:post
         {:x-name ::update-data
          :parameters {:body-params ::update}
          :handler (fn [request]
                     (println "****** DATA FROM request:")
                     (prn (:body-params request))
                     (ok "Expecting error"))}})))))


