# Kafka Connector 예제

## Build

```
# mvn clean package
```

## Kafka Connector 관련 속성

### 공통 속성

```
bootstrap.servers=localhost:9092
key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
tasks.max=1
schema.registry.url=http://localhost:8081
poll.interval.ms=5000
```