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

### Kafka Connector 관련 속성

#### 공통 속성

```
bootstrap.servers=localhost:9092
key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
tasks.max=1
schema.registry.url=http://localhost:8081
```

### 분산 모드로 실행

분산 모드로 실행하기 위하서 다음과 같이 환경설정 파일을 구성합니다.

```
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

다음의 커맨드 라인으로 Connector를 실행합니다.

```
bin/connect-distributed.sh config/connect-distributed.properties
```

Connector를 배포하기 위해서 다음과 같이 JSON 파일을 작성합니다.

```
{
    "name": "my-source-connector",
    "config": {
        "connector.class": "org.apache.kafka.connect.file.FileStreamSourceConnector",
        "tasks.max": "1",
        "file": "/path/to/input/file",
        "topic": "my-topic"
    }
}
```

REST API를 호출하여 Connector를 생성합니다.

```
# 커넥터 생성 요청
curl -X POST -H "Content-Type: application/json" --data @config/connector-name.json http://localhost:8083/connectors
```