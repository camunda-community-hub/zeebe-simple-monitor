# Usage

Start docker containers:

```
docker-compose pull
docker-compose up
```

Create default-topic using [zbctl](https://github.com/zeebe-io/zbc-go/releases).

```
zbctl create topic --name default-topic --partitions 1
```

Go to http://localhost:9000 and connect to broker `zeebe:51015`.
