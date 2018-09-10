Reproduce with:

```
clj
lein ring server-headless
curl -H "Content-type: application/json" -d '{"g/sp": "s1", "g/cn": "cn1", "g/bt": "XXXX"}' http://localhost:3000/issue
```
