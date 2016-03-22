(ns datadog.core-test
  (:require [clojure.test :refer :all]
            [datadog.core :as dd]
            [expectations :refer [expect side-effects]]))

(defmacro last-sent-msg
  "Returns last data that was sent to `dd/send-msg`
   during execution of the body"
  [& body]
  `(let [sended# (atom nil)]
     (with-redefs
       [dd/send-msg (fn [value#] (reset! sended# value#))]
       ~@body
       @sended#)))

(deftest should-format-tags
  (expect "|#site:main"
          (dd/format-tags {:site :main}))
  (expect "|#page:contacts"
          (dd/format-tags {:page "contacts"}))
  (expect "|#status:200"
          (dd/format-tags {:status 200}))
  (expect "|#error"
          (dd/format-tags {:error nil}))
  (expect "|#site:main,page:contacts,status:200"
          (dd/format-tags {:site :main, :page "contacts", "status" 200})))

(deftest should-send-increment
  (expect "page.views:1|c"
          (last-sent-msg (dd/increment "page.views")))
  (expect "page.views:7|c"
          (last-sent-msg (dd/increment "page.views" 7)))

  (expect "page.views:1|c|#page:contacts"
          (last-sent-msg (dd/increment "page.views" {:page "contacts"})))
  (expect "page.views:8|c|#page:contacts"
          (last-sent-msg (dd/increment "page.views" 8 {:page "contacts"}))))

(deftest should-send-decrement
  (expect "users.online:-1|c"
          (last-sent-msg (dd/decrement "users.online")))
  (expect "users.online:-3|c"
          (last-sent-msg (dd/decrement "users.online" 3)))
  (expect "users.online:-1|c|#group:admins"
          (last-sent-msg (dd/decrement "users.online" {:group "admins"})))
  (expect "users.online:-5|c|#group:admins"
          (last-sent-msg (dd/decrement "users.online" 5 {:group "admins"}))))

(deftest should-send-gauge
  (expect "total.posts:526|g"
          (last-sent-msg (dd/gauge "total.posts" 526)))
  (expect "total.posts:526|g|#site:main"
          (last-sent-msg (dd/gauge "total.posts" 526 {:site "main"}))))

(deftest should-send-set
  (expect "users.unique:653848|s"
          (last-sent-msg (dd/unique "users.unique" 653848)))
  (expect "users.unique:653848|s|#group:providers"
          (last-sent-msg (dd/unique "users.unique" 653848 {:group "providers"}))))

(deftest should-send-timings
  (expect "db.query.time:576|ms"
          (last-sent-msg (dd/timing "db.query.time" 576)))
  (expect "db.query.time:843|ms|#query:find-by-id"
          (last-sent-msg (dd/timing "db.query.time" 843 {:query "find-by-id"}))))

;; We tested that `dd/timing` sends data correctly
;; So we know it works in next test, so we wont chack value
(deftest should-measure-time
  (expect #"something.done:\d*\|ms\|#tag:present"
          (last-sent-msg (dd/timed "something.done" {:tag "present"}
                                   (do (str "Whatever passed -"
                                            "is passed")))))
  (expect #"something.done:\d*\|ms"
          (last-sent-msg (dd/timed "something.done" {}
                                   (do (str "Whatever passed -"
                                            "is passed"))))))
