(ns default-transformer-stripping-data-issue.handler
  (:require
   [clojure.spec.alpha :as s]
   [compojure.api.coercion.spec :as spec-coercion]
   [compojure.api.sweet :refer [api context resource routes]]
   [ring.util.http-response :refer [bad-request]]
   [spec-tools.core :as stc]
   [spec-tools.transform :as stt]))

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

;;; hacks to workaround stripping spec-data stripping behaviour in compojure.api
(def string-transformer
  (stc/type-transformer
    {:name :string
     :decoders stt/string-type-decoders
     :encoders stt/string-type-encoders
     :default-encoder stt/any->any}))

(def json-transformer
  (stc/type-transformer
    {:name :json
     :decoders stt/json-type-decoders
     :encoders stt/json-type-encoders
     :default-encoder stt/any->any}))

(defn non-stripping-spec-keys-coercion []
  (let [mimetypes (-> spec-coercion/default-options :body keys)
        options (-> spec-coercion/default-options
                    (assoc-in
                     [:body :formats]
                     (zipmap mimetypes (repeat json-transformer)))
                    (assoc-in [:body :string :default] string-transformer))]
    (spec-coercion/create-coercion options)))

(defn expect-request-validation-to-fail [request]
  (bad-request "Expecting request validation to fail"))

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Default-transformer-stripping-data-issue"
                    :description "Compojure Api example"}
             :tags [{:name "api", :description "some apis"}]}}}
    (routes
     (context "/solution-1" []
       :coercion (non-stripping-spec-keys-coercion)
       (resource
        {:post
         {:x-name ::update-data-custom-coercion-fix
          :parameters {:body-params ::update}
          :handler expect-request-validation-to-fail}}))
     (context "/issue" []
       :coercion :spec
       (resource
        {:post
         {:x-name ::update-data-failing
          :parameters {:body-params ::update}
          :handler expect-request-validation-to-fail}})))))



