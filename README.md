### Synopsis

A spec as defined in `handler.clj` will fail (as expected) at the repl when testing with data:

```clojure
(require '[default-transformer-stripping-data-issue.handler :as h])
(s/valid? ::h/update {:g/sp "s1" :g/cn "cn1" :g/bt "XXXX"})
```

but not when run through a compojure.api application, using `:spec` coercion.


Reproduce with:

```
clj
lein ring server-headless
curl -H "Content-type: application/json" -d '{"g/sp": "s1", "g/cn": "cn1", "g/bt": "XXXX"}' http://localhost:3000/issue
```

Proposed solution is to apply custom coercion, 
modifying the default coercion options to define replacement transformers:

```bash
curl -H "Content-type: application/json" -d '{"g/sp": "s1", "g/cn": "cn1", "g/bt": "XXXX"}' http://localhost:3000/solution-1
```
