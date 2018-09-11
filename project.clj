(defproject default-transformer-stripping-data-issue "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :plugins
  [[lein-environ "1.1.0"]
   [lein-pprint "1.1.2"]
   [lein-ring "0.12.0"]
   [lein-tools-deps "0.4.1"]]
  :lein-tools-deps/config {:config-files [:install :user :project]}
  :ring {:handler default-transformer-stripping-data-issue.handler/app}
  :uberjar-name "server.jar"
  :source-paths ["src"]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :profiles
  {:dev
   {:lein-tools-deps/config {:resolve-aliases [:dev]}
    :plugins
    [[refactor-nrepl "2.4.0"]
     [cider/cider-nrepl "0.18.0"]]}})

