(ns datadog.core
  "A simple StatsD client written in Clojure
   Taken from https://github.com/etsy/statsd/tree/master/examples

   Comments for metrics are taken from official documentation:
   http://docs.datadoghq.com/guides/dogstatsd"
  (:import (java.net InetAddress DatagramPacket DatagramSocket))
  (:gen-class))

;; Default connection parameters

(def ^:private connection (atom {:host "127.0.0.1"
                                 :port 8125}))

(defn set-connection!
  "External function to let user redefine connection map"
  [conn]
  (reset! connection conn))

(defn format-tags
  "Construcsts statsd-formatted string with tags"
  [tags]
  (if (empty? tags)
    ""
    (let [format-pair (fn [[key value]]
                        (str (name key)
                             (if (some? value) ":")
                             (if (keyword? value) (name value) (str value))))
        tags-list (map format-pair tags)
        reduced-tags (clojure.string/join "," tags-list)]
      (str "|#" reduced-tags))))

;; UDP helper functions

(defn- make-socket
  ([] (new DatagramSocket))
  ([port] (new DatagramSocket port)))

(defn- send-data [send-socket ip port data]
  (let [ip-address (InetAddress/getByName ip),
        send-packet (new DatagramPacket
                         (.getBytes data)
                         (.length data)
                         ip-address
                         port)]
  (.send send-socket send-packet)))

(defn- make-send [ip port]
  (let [send-socket (make-socket)]
       (fn [data] (send-data send-socket ip port data))))

(def ^:private send-msg (make-send (:host @connection) (:port @connection)))

;; StatsD client functions

(defn increment
  "Counters track how many times something happened per second,
   like the number of database requests or page views."
  ([metric] (increment metric 1))
  ([metric arg] (if (map? arg)
                  (increment metric 1 arg)
                  (increment metric arg {})))
  ([metric value tags]
   (send-msg (str metric ":" value "|c" (format-tags tags)))))

(comment
  (increment "database.query.count")
  (increment "page_views.count" 10 {:page "contacts"}))

(defn decrement
  "Shorthand for incrementing by negative values."
  ([metric] (increment metric -1))
  ([metric arg] (if (map? arg)
                  (decrement metric 1 arg)
                  (decrement metric arg {})))
  ([metric value tags] (increment metric (- value) tags)))

(comment
  (decrement "comments")
  (decrement "users.online" {:group "administrators"}))

(defn gauge
  "Histograms track the statistical distribution of a set of
   values, like the duration of a number of database queries
   or the size of files uploaded by users. Each histogram will
   track the average, the minimum, the maximum, the median,
   the 95th percentile and the count."
  ([metric value] (gauge metric value {}))
  ([metric value tags]
   (send-msg (str metric ":" value "|g" (format-tags tags)))))

(comment
  (gauge "file.upload.size" (:size file))
  (gauge "file.size" (:size file) {:type "upload"}))

(defn unique
  "Sets are used to count the number of unique elements in
   a group. If you want to track the number of unique
   visitor to your site, sets are a great way to do that."
  ([metric value] (unique metric value {}))
  ([metric value tags]
    (send-msg (str metric ":" value "|s" (format-tags tags)))))

(comment
  (unique "users.uniques" (:id user))
  (unique "sessions.unique" (:id session) {:group (:group session)}))

(defn timing
  "StatsD only supports histograms for timing, not generic values
   (like the size of uploaded files or the number of rows returned
   from a query). Timers are essentially a special case of histograms,
   so they are treated in the same manner by DogStatsD for
   backwards compatibility.

   You might consider using macro `timed`"
  ([metric value] (timing metric value {}))
  ([metric value tags]
   (send-msg (str metric ":" value "|ms" (format-tags tags)))))

(comment
  (timing "services.api" elapsed)
  (timing "databse.query.time" elapsed {:query "filtered"}))

(defmacro timed
  "Measure time of the execution of the provided body parts,
   and then report measured time.
   Returns value returned by last expression."
  [metric tags & body]
  `(let [start# (System/currentTimeMillis)
         result# (do ~@body)
         time# (- (System/currentTimeMillis) start#)]
     (timing ~metric time# ~tags)
     result#))

(comment
  (timed "database.query.time" {:query "find-by-id"}
         (db.find-by-id id)))

