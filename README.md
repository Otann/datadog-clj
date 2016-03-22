# datadog-clj [![Circle CI](https://circleci.com/gh/Otann/datadog-clj.svg?style=shield&no-cache)](https://circleci.com/gh/Otann/datadog-clj)

`datadog-clj` is a client for [DataDog](https://www.datadoghq.com) service
for the [Clojure](http://clojure.org) programming language.

[![Clojars Project](http://clojars.org/datadog/latest-version.svg)](https://clojars.org/datadog)

## Origins

This was a project I developed for TruckerPath company, while I was working there. 
Due to the other developer uncooperative nature, I decided to
continue working in a forked repository under a different name.

## Installation

Add `[datadog "1.0.0"]` to the dependency section in your project.clj file:

## Example Usage

To start tracking events and sending them to DataDog, you firstly have
to register account and install
[DataGog agent](http://docs.datadoghq.com/guides/basic_agent_usage/)
that will start StatsD server on the machine you are running your
application.

Then import DataDog in your namespace:

```clojure
(require '[datadog.core :as dd])
```

If you are using an alternative installation, you can easily redefine the 
default connection to the agent with the following code:

```clojure
(dd/set-connection! {:host "datadog" :port 31337})
```

Following DataDog metric functions are available:

### Counters

You can use either amount or DataDog tags or both.
Decrements are completely symmetrical to increments but
with negative values.

```clojure
(dd/increment "page.views")
(dd/increment "page.views" 10)
(dd/increment "error.count" {:page "products"})
(dd/increment "active.connections" 3 {:service "db"})

(dd/decrement "users.online")
(dd/decrement "users.online" {:group "admins"})
```

### Gauges

Gauges require value to be specified, but tags can be omitted

```clojure
(dd/gauge "total.posts" 526)
(dd/gauge "total.posts" 526 {:site "main"})
```

### Sets

Gauges require value to be specified, but tags can be omitted

```clojure
(dd/gauge "total.posts" 526)
(dd/gauge "total.posts" 526 {:site "main"})
```

### Timers

You can report time directly or using a macro that
will do reporting as a side-effect.

In second case tags are required, even if empty.

```clojure
(dd/timing "db.query.time" 843 {:query "find-by-id"})
(dd/timed "external.service.call" {:service service}
          (http/get remote-uri {:socket-timeout timeout}))
```          


## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
