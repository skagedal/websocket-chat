# Chat

## Redis

Start up redis:

```shell
docker-compose up
```

Try stuff:
```shell
redis-cli -p 10000 subscribe chat
```
Other shell:
```shell
redis-cli -p 10000 publish chat hello
```

## Vert.x
Run App in IntelliJ.

Then:
```shell
websocat ws://127.0.0.1:10001/chat
```