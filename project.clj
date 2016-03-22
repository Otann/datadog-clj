(defproject datadog-clj "1.0.0"
  :description "Clojure client for DataDog service via statsd protocol"
  :url "https://github.com/otann/datadog-clj"

  :dependencies [[org.clojure/clojure "1.6.0"]]

  :profiles {:uberjar {:aot :all}
             :test {:dependencies [[expectations "2.0.9"]]}}

  ;; Artifact deployment info
  :scm {:name "git"
        :url "https://github.com/otann/datadog-clj"}

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :pom-addition [:developers [:developer
                              [:name "Anton Chebotaev"]
                              [:url "http://otann.com"]
                              [:email "anton.chebotaev@gmail.com"]
                              [:timezone "+3"]]])
