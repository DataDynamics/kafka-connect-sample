# Kafka Connector 예제

## Build

```
# mvn clean package
```

## Kafka Connector

### 장점

* 확장성 - 클러스터 형태로 동작하여, 데이터 스트리밍 워크로드 실행 가능
* 단순성 - 간단한 설정을 통해 Source, Sink 설정 가능
* 유연성 - 다양한 Source, Sink 연결 가능
* 내결함성 - 내결함성, 재시도 매커니즘 제공 (장애 발생시 데이터 손실)
* 확장 가능 커넥터 - 다양한 커넥터 제공, 커넥터 개발 가능
* 데이터 변환 - Data Transformation 제공
* 관리 및 모니터링 - REST API를 통해 가능 (상태 조회, 설정 변경 등)

### Source Connector

* 멀티 스레드
* 외부 리소스에서 데이터를 읽어서 Kafka Topic으로 전송

### Sink Connector

* 단일 쓰레드
* Kafka Topic에서 데이터를 수신해서 외부 리소스에 데이터를 전송

### 동작 모드

* 스탠드얼론 모든
  * 단일 인스턴스로 실행(클러스터 불필요)
  * 구성 파일의 설정이 단순
  * 빠르게 구성하고 테스트 가능
  * 확장성 부족
* 분산 모드
  * 여러 Task을 클러스터로 실행
  * 장애 발생시 타 Task가 대신 수행
  * 중앙 집중식 관리 (REST API)

### Kafka 설정

테스트를 위해서 Kafka를 다운로드하고 다음과 같이 환경을 구성합니다.

```
# DOWNLOAD KAFKA
# wget https://archive.apache.org/dist/kafka/3.4.1/kafka_2.12-3.4.1.tgz
# tar xvfz kafka_2.12-3.4.1.tgz
# cd kafka_2.12-3.4.1/bin

# RUN ZK
# sh zookeeper-server-start.sh ../config/zookeeper.properties

# RUN KAFKA
# sh kafka-server-start.sh ../config/server.properties

# CREATE TOPIC
# sh kafka-topics.sh --create --topic mytopic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

# LIST TOPICS
# sh kafka-topics.sh --list --bootstrap-server localhost:9092
```

### Kafka Connector 관련 속성

#### 공통 속성

Kafka Connector에서 사용하는 공통 속성은 다음과 같습니다(예; `demo-connect-standalone.properties`). 

```properties
bootstrap.servers=localhost:9092
key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
tasks.max=1
schema.registry.url=http://localhost:8081
```

### Standalone 모드로 실행

테스트를 위해서 `demo-connect-standalone.properties` 파일을 다음과 같이 작성했습니다.

```properties
bootstrap.servers=localhost:9092

key.converter=org.apache.kafka.connect.storage.StringConverter
value.converter=org.apache.kafka.connect.storage.StringConverter

key.converter.schemas.enable=true
value.converter.schemas.enable=true

offset.storage.file.filename=/temp/connect.offsets
offset.flush.interval.ms=10000

rest.port=8083

plugin.path=/Users/fharenheit/Projects/kafka/kafka/libs
```

이제 Kafka Connector의 설정 파일을 다음과 같이 작성합니다(예; `demo-connect-file-sink.properties`).

```properties
name=local-file-sink
# 아래 connector.class 설정 모두 적용됨
#connector.class=FileStreamSink
connector.class=org.apache.kafka.connect.file.FileStreamSinkConnector
tasks.max=1
file=/temp/test.sink.txt
topics=mytopic
```

다음의 커맨드 라인으로 Connector를 실행합니다(Standalone, Distributed 모드에 따라서 다르게 실행할 수 있습니다).

```bash
# STANDALONE MODE
# bin/connect-standalone.sh ../config/demo-connect-standalone.properties ../config/demo-connect-file-sink.properties 

# DISTRIBUTED MODE
# bin/connect-distributed.sh ../config/demo-connect-distributed.properties ../config/demo-connect-file-sink.properties
```

Kafka Connector의 `.properties` 파일을 Kafka Connector 실행시 지정하지 않는 경우 다음과 같이 REST API로 등록할수 있습니다.
Connector를 배포하기 위해서 다음과 같이 JSON 파일을 작성합니다.

```json
{
    "name": "my-source-connector",
    "config": {
        "connector.class": "org.apache.kafka.connect.file.FileStreamSourceConnector",
        "tasks.max": "1",
        "file": "/path/to/input/file",
        "topic": "mytopic"
    }
}
```

REST API를 호출하여 Connector를 생성합니다.

```bash
# 커넥터 생성 요청
# curl -X POST -H "Content-Type: application/json" --data @config/connector-name.json http://localhost:8083/connectors
```

테스트를 위해서 Kafka에 내장되어 있는 Console Producer를 이용하여 메시지를 송신합니다.

```bash
# cd <KAFKA_HOME>/bin
# sh kafka-console-producer.sh --topic mytopic --bootstrap-server localhost:9092
```

### 분산 모드로 실행

분산 모드로 실행하기 위해서 다음과 같이 환경설정 파일을 구성합니다(예; `demo-connect-distributed.properties`).

```properties
# config/connect-distributed.properties

# Bootstrap servers for Kafka 클러스터에 연결하기 위한 브로커 주소들
bootstrap.servers=localhost:9092

# Distributed worker ID
worker.id=connect-1

# 그룹 ID (연결된 작업자들을 구분하기 위한 그룹 ID)
group.id=connect-cluster

# Kafka 내부 주제 (Connect 작업 상태, 오프셋, 설정 등을 저장하기 위한 주제)
config.storage.topic=connect-configs
offset.storage.topic=connect-offsets
status.storage.topic=connect-status

# 각 주제의 복제 계수
config.storage.replication.factor=1
offset.storage.replication.factor=1
status.storage.replication.factor=1

# REST API 포트 (작업자 관리, 모니터링 등을 위한 API 엔드포인트)
rest.port=8083
```

나머지 설정은 Standalone 모드와 동일합니다.

## 트러블 슈팅

### `Failed to find any class that implements Connector and which name matches FileStreamSink`

Kafka Connector 클래스를 못찾는 현상으로써 Kafka Connector는 plugin이므로 plugin의 경로를 지정해야 합니다.
Kafka Connector를 실행시키기 위해서 필요한 환경설정 파일 (예; `connect-standalone.properties`)에 다음과 같이 Kafka Connector를 포함하는 JAR 파일을 지정합니다.

```properties
plugin.path=libs/connect-file-3.4.0.jar
```

기본 설정 파일 템플릿에는 다음과 같이 설명이 추가되어 있습니다. 디렉토리를 지정하거나, JAR 파일을 지정할 수 있습니다.

```properties
# Set to a list of filesystem paths separated by commas (,) to enable class loading isolation for plugins
# (connectors, converters, transformations). The list should consist of top level directories that include 
# any combination of: 
# a) directories immediately containing jars with plugins and their dependencies
# b) uber-jars with plugins and their dependencies
# c) directories immediately containing the package directory structure of classes of plugins and their dependencies
# Note: symlinks will be followed to discover dependencies or plugins.
# Examples: 
# plugin.path=/usr/local/share/java,/usr/local/share/kafka/plugins,/opt/connectors,
#plugin.path=
```

## 참고

* [Deploy Kafka Connect](https://developer.confluent.io/courses/kafka-connect/deployment/?utm_medium=sem&utm_source=google&utm_campaign=ch.sem_br.nonbrand_tp.prs_tgt.dsa_mt.dsa_rgn.apac_lng.eng_dv.all_con.confluent-developer&utm_term=&creative=&device=c&placement=&gad_source=1&gclid=CjwKCAjw9eO3BhBNEiwAoc0-jYXuADg1BhBHF88VmwlvE272B0wlrcnPTnb9ZIVV3kP8CqQRZDHNcRoCFhQQAvD_BwE)
* [How to Use Kafka Connect - Get Started](https://docs.confluent.io/platform/current/connect/userguide.html)
* [Apache Kafka Guide #51 Kafka Connect: Standalone vs Distributed Mode](https://medium.com/apache-kafka-from-zero-to-hero/apache-kafka-guide-51-kafka-connect-standalone-vs-distributed-mode-e4486eb4074f#id_token=eyJhbGciOiJSUzI1NiIsImtpZCI6IjVhYWZmNDdjMjFkMDZlMjY2Y2NlMzk1YjIxNDVjN2M2ZDQ3MzBlYTUiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiIyMTYyOTYwMzU4MzQtazFrNnFlMDYwczJ0cDJhMmphbTRsamRjbXMwMHN0dGcuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiIyMTYyOTYwMzU4MzQtazFrNnFlMDYwczJ0cDJhMmphbTRsamRjbXMwMHN0dGcuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDU5OTk0NTcwMzk1MzY5ODI3MDciLCJlbWFpbCI6ImZoYXJlbmhlaXRAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5iZiI6MTcyNzYxMDkwNywibmFtZSI6Iuq5gOuzkeqzpCIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQ2c4b2NKREZJb0pabWJFdHNQUC1NUGJQVHU0UzBlSGpxWHV5czloN0hHeTVuRG41aFlVeUJzdj1zOTYtYyIsImdpdmVuX25hbWUiOiLrs5Hqs6QiLCJmYW1pbHlfbmFtZSI6Iuq5gCIsImlhdCI6MTcyNzYxMTIwNywiZXhwIjoxNzI3NjE0ODA3LCJqdGkiOiI5NzFjMDU4ZjJmZGE0MzM2YTQyYWY1YzlkZjBiYTUxMDg3ZWNmNjE0In0.HZ3tJGzwGAl0bJeX0mK-VL-YGqMCEXRmF9vEBTWp2S0jR75LFs03pEKCnYHb8WAxF0YsR4Ngxv6DgAVDEzONyIFeP_k-jdZ34jJx8K0TWNIalQyZPDzhXQMpj8kz3owWyrfOI-LHOyQEY6npa1vKtFuZzuctsowrv_fike0uP7IWV2uNrwsHfhEOHFrohIzA9cy7eLSsxy2D9NgCDuM-57Y5RpuNnUV5l_6_e8b3TtTmJbprl-A6ZVusGkdEirhYcLCLqCCLqcyJkO06R-LuiV35oTERuudBvU57W00T2jsY9z-rw433QGPr6IsAAdV8JQJxFFysJyXYRYA8Y7sWjw)
* [Kafka Connect CLI Tutorial](https://learn.conduktor.io/kafka/kafka-connect-cli-tutorial/)
* [How to Install and Run a Custom Connector](https://github.com/enfuse/kafka-connect-demo/blob/master/docs/install-connector.md)
* [(YouTube) Kafka Connect Standalone Mode Example](https://www.youtube.com/watch?app=desktop&v=G6YlWrKZK0E)