(ns default-transformer-stripping-data-issue.handler
  (:require
   [clojure.spec.alpha :as s]
   [clojure.pprint :refer [pprint]]
   [compojure.api.coercion.core :as cc]
   [compojure.api.coercion.spec :as spec-coercion]
   [compojure.api.sweet :refer [api context resource routes]]
   [muuntaja.core :as m]
   [ring.util.http-response :refer [bad-request ok]]
   [spec-tools.core :as stc]
   [spec-tools.spec :as spec]
   [spec-tools.transform :as stt]
   [spec-tools.swagger.core :as swagger]))

(s/def :g/sp spec/keyword?)
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
                             :opt [:p/when
                                   :p/who
                                   :p/how
                                   :p/why])
                     (s/map-of
                      #{:g/sn
                        :g/bt
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

(s/def ::kw-test (stc/spec (s/keys :req [:g/sp])))

(s/def ::update (stc/spec (s/or :v-a ::variant-a :v-b ::variant-b)))

(defn expect-request-validation-to-fail-on-extra-keys [request]
  (bad-request {:message (str "Expecting request validation to fail with: "
                              "compojure.api.exception/request-validation"
                              "but did not")
                :body-params (:body-params request)}))

;;; transformers that dont' apply key stripping decoders
;;;
;;; (these functions are otherwise verbatim copies of
;;; compojure.api.coercion.spec/{string-transformer,json-transformer})

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
                     {"application/json" json-transformer
                     "application/msgpack" json-transformer
                     "application/x-yaml" json-transformer})
                    (assoc-in [:body :string :default] string-transformer))]
    (spec-coercion/create-coercion options)))

(defn keyword-coercion [request]
  (ok {:message "Keywords coerced in body-params ok"
       :data (:body-params request)}))

(def custom-coercion (non-stripping-spec-keys-coercion))

(defmethod cc/named-coercion ::spec [_] custom-coercion)

(def app
  (api   
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
      :data {:info {:title "Default-transformer-stripping-data-issue"
                    :description "Compojure Api example"}
             :tags [{:name "api", :description "some apis"}]}}}
    (routes
     (context "/kw-test-custom-spec-coercion" []
       :coercion ::spec
       (resource
        {:post
         {:x-name ::kw-test-custom-spec-coercion
          :parameters {:body-params ::kw-test}
          :handler keyword-coercion}}))
     (context "/kw-test-default-spec-coercion" []
       :coercion :spec
       (resource
        {:post
         {:x-name ::kw-test-default-spec-coercion
          :parameters {:body-params ::kw-test}
          :handler keyword-coercion}}))     
     (context "/solution-1" []
       :coercion ::spec
       (resource
        {:post
         {:x-name ::update-data-custom-coercion-fix
          :parameters {:body-params ::update}
          :handler expect-request-validation-to-fail-on-extra-keys}}))
     (context "/issue" []
       :coercion :spec
       (resource
        {:post
         {:x-name ::update-data-failing
          :parameters {:body-params ::update}
          :handler expect-request-validation-to-fail-on-extra-keys}})))))



