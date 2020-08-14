# funnel-client

<!-- badges -->
[![CircleCI](https://circleci.com/gh/lambdaisland/funnel-client.svg?style=svg)](https://circleci.com/gh/lambdaisland/funnel-client) [![cljdoc badge](https://cljdoc.org/badge/lambdaisland/funnel-client)](https://cljdoc.org/d/lambdaisland/funnel-client) [![Clojars Project](https://img.shields.io/clojars/v/lambdaisland/funnel-client.svg)](https://clojars.org/lambdaisland/funnel-client)
<!-- /badges -->

Clojure and ClojureScript client libraries for [Funnel](https://github.com/lambdaisland/funnel)

<!-- opencollective -->

&nbsp;

<img align="left" src="https://github.com/lambdaisland/open-source/raw/master/artwork/lighthouse_readme.png">

&nbsp;

## Support Lambda Island Open Source

funnel-client is part of a growing collection of quality Clojure libraries and
tools released on the Lambda Island label. If you are using this project
commercially then you are expected to pay it forward by
[becoming a backer on Open Collective](http://opencollective.com/lambda-island#section-contribute),
so that we may continue to enjoy a thriving Clojure ecosystem.

&nbsp;

&nbsp;

<!-- /opencollective -->

<!-- installation -->
## Installation
deps.edn

```
lambdaisland/funnel-client {:mvn/version "0.0.0"}
```

project.clj

```
[lambdaisland/funnel-client "0.0.0"]
```
<!-- /installation -->

## Rationale

To use Funnel all you need is a websocket connection and a way to read and write
Transit. As such we originally envisaged people would use their websocket
connection of choice to connect to Funnel directly.

To reduce the boilerplate that people have to write, and to encode some best
practice patterns, we decided to make these clients available. There's a Clojure
and a ClojureScript client, which both implement the same high level API, to the
extent possible, providing that they sit upon very different websocket
implementations and platforms.

## Usage

At it simplest all you need is:

``` clojure

(require '[lambdaisland.funnel-client :as funnel-client])

(def conn (funnel-client/connect {:on-message prn}))

;; query funnel to get the whoami map of every connected client
(funnel-client/send conn {:funnel/query true})
```

See the [Funnel README](https://github.com/lambdaisland/funnel) to understand
how to use the Funnel protocol.

`connect` takes the following options

- `:uri` defaults to `ws://localhost:44220`, or if using the ClojureScript client and the current origin is https, then it will use `wss` (SSL)
- `:on-open` / `:on-error` / `:on-close` / `:on-message` callbacks, these all take two arguments, the connection itself, and a handshake/error/message
- `:whoami` map or atom containing the Funnel whoami map. If omitted will use the `funnel-client/whoami` global atom.

If using an atom as a `:whoami` then changing the contents of the atom will
automatically re-announce the identifying information to Funnel.

### Exclusions

If you only use one of the two clients, you may want to exclude the dependencies needed for the other client.

For ClojureScript we pull in

- `com.cognitect/transit-cljs`
- `lambdaisland/glogi`

On Clojure we make use of

- `io.pedestal/pedestal.log`
- `org.java-websocket/Java-WebSocket`
- `com.cognitect/transit-clj`

<!-- contributing -->
## Contributing

Everyone has a right to submit patches to funnel-client, and thus become a contributor.

Contributors MUST

- adhere to the [LambdaIsland Clojure Style Guide](https://nextjournal.com/lambdaisland/clojure-style-guide)
- write patches that solve a problem. Start by stating the problem, then supply a minimal solution. `*`
- agree to license their contributions as MPL 2.0.
- not break the contract with downstream consumers. `**`
- not break the tests.

Contributors SHOULD

- update the CHANGELOG and README.
- add tests for new functionality.

If you submit a pull request that adheres to these rules, then it will almost
certainly be merged immediately. However some things may require more
consideration. If you add new dependencies, or significantly increase the API
surface, then we need to decide if these changes are in line with the project's
goals. In this case you can start by [writing a pitch](https://nextjournal.com/lambdaisland/pitch-template),
and collecting feedback on it.

`*` This goes for features too, a feature needs to solve a problem. State the problem it solves, then supply a minimal solution.

`**` As long as this project has not seen a public release (i.e. is not on Clojars)
we may still consider making breaking changes, if there is consensus that the
changes are justified.
<!-- /contributing -->

<!-- license -->
## License

Copyright &copy; 2020 Arne Brasseur and Contributors

Licensed under the term of the Mozilla Public License 2.0, see LICENSE.
<!-- /license -->
