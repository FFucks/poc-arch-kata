# poc-arch-kata

# Possible languages:

1 - Golang

    Pros: low lattency, simple runtimes, stable binary, excelent for concurrent I/O. Good gRPC library.
    
    Cons: less abstractions “reactive” than Java.

2 - Java / Kotlin (Spring WebFlux / Netty / Akka-Pekko)

    Pros: rich ecosystem (Kafka, Flink, Cassandra), JVM mature, good reactive framework support (Project Reactor), good observability tools.
    
    Cons: tunning of GC/heap, bigger images.

3 - Scala (Akka / Pekko, Kafka Streams)

    Pros: great to modeling reactive systems, actor model for websocket and stream processor, good librarys for exactly-once.
    
    Cons: high learning curve, build complexity.

4 - Rust

    Pros: maximum performance and latency control, low allocation.
    
    Cons: less mature ecosysstem on some integrations (Kafka clients, etc.), high learning curve.

5 - Node.js / Deno

    Pros: fast development, good for prototypes and a lot of WebSockets with low CPU.
    
    Cons: single-threaded — not great with intensive CPUusage; GC pauses can inpact.

# Metrics:

1 - Ingest latency: ≤ 50 ms (time between client send → server ack that event was durably written)

2 - Processing latency: ≤ 200 ms (commit log → materialized store update)

3 - Fanout latency: ≤ 200 ms (store update → clients receive update)

4 - End-to-end (E2E): ≤ 500 ms p95, ≤ 1 s p99



# Possible tools to use:

1 - Metrics: Prometheus + Grafana

2 - Distributed tracing: OpenTelemetry → Jaeger / Time

3 - Structured logs: ELK (Elasticsearch) / Loki + Grafana

4 - Profiling / live diagnostics: eBPF tools (bpftools), async-profiler (JVM), pprof (Go)

5 - RUM / Synthetic: Synthetics (Grafana synthetic, Pingdom) + Real User Monitoring (e.g., New Relic Browser) for latency.



# Checklist to have realtime:

1 - Trace-id created by the client roam all the pipeline ?

2 - Metrics E2E p50/p95/p99 are show in Grafana?

3 - Synthetic test sent one vote and watch the client update on a valid SLA, 1000x / hour ?

4 - Load tests shows stability with 250k RPS with latency insisde the SLO ?

5 - Consumer lag maintain low value (configurated) in p99?

6 - Trigger alert when latency/p99 rises above SLO ?

7 - Do chaos tests cause no data loss or latency violations without failover?


# X-ray vs Jaeger

X-ray:
    Fully managed
    No cluster to maintain, no upgrades, no tuning.
    Deep integration with AWS
    Automatic service maps
    Very simple for those who are 100% AWS
 - Automatically instruments:
    API Gateway
    Lambda
    DynamoDB
    SQS / SNS
    ECS / EKS
    ALB / ELB

Jaeger:
    Open-source, CNCF standard
    High compatibility with OpenTelemetry
    Ideal for hybrid or self-hosted environments
    No vendor lock-in
    More flexible
 - Pluggable with:
    Grafana
    Prometheus
    Tempo
    Elastic
    Kafka

# Endpoints:

### <span style='color:#3BC143 ;font-weight: bold;'>AUTHENTICATION</span>
method: <span style='color:#FFBE33;font-weight: bold;'>POST</span>
path: <span style='color:#FFBE33;font-weight: bold;'>v1/auth/login</span>
- User authentication endpoint usually used to identify the current user session and fetch user data. Response must return the userId and user token.
    1. username and password are required fields
    - request
  ```json
  {
    username : "string",
    password : "string"    
  }
  ```
    - response
  ```json
  {
    userId : "string",
    token : "string"
  }
  ```

### <span style='color:#3BC143 ;font-weight: bold;'>REGISTRATION</span>
method: <span style='color:#FFBE33;font-weight: bold;'>POST</span>
path: <span style='color:#FFBE33;font-weight: bold;'>v1/auth/register</span>
- User registration endpoint used to create a new user in the system. Response must return the userId.
    1. Authorization header with Bearer token is required
    2. Fields username, email and dateOfBirth are required
    3. Response code success must be 201 Created
    4. Response code failure for invalid fields must be 400 Bad Request
    5. Response code failure for unauthorized must be 401 Unauthorized
    6. Response code failure for user not found must be 404 Not Found
    - headers
  ```json
  {
    Authorization : Bearer "string"
  }
  ```
    - request
  ```json
  {
    username : "string",
    email : "string",
    date_of_birth : "string"
  }
  ```
    - response
  ```json
  {
    user_id : "string"
  }
  ```

### <span style='color:#3BC143 ;font-weight: bold;'>CREATE EVENT</span>
method: <span style='color:#FFBE33;font-weight: bold;'>POST</span>
path: <span style='color:#FFBE33;font-weight: bold;'>v1/event/create</span>
- Endpoint to create a new voting event. Response must return the eventId and the list of contestants created.
    1. Authorization header with Bearer token is required
    2. Fields user_id, event_id, contestant_id are required
    3. Response code success must be 200 OK
    4. Response code failure for invalid fields must be 400 Bad Request
    5. Response code failure for unauthorized must be 401 Unauthorized
    6. Response code failure for contestant_id and/or event_id not found must be 404 Not Found
    - headers
  ```json
  {
    Authorization : Bearer "string"
  }
  ```
    - request
  ```json
  {
    "user_id": "string",
    "event_name": "string",
    "contestants": [
      {
        "contestant_name": "string",
        "contestant_description": "string",
        "contestant_image_url": "string"
      }
    ]
  }
  ```
    - response
  ```json
  {
    "event_id" : "string",
    "contestants": [
      {
        "contestant_id": "string",
        "contestant_name": "string"
      }
    ]
  }
  ```

### <span style='color:#3BC143 ;font-weight: bold;'>GET EVENT LEADERBOARD</span>
method: <span style='color:#FFBE33;font-weight: bold;'>GET</span>
path: <span style='color:#FFBE33;font-weight: bold;'>v1/event/{event_id}/leaderboard</span>
- Endpoint to get the leaderboard of a given event. Response must return the event_id, event_name and the list of contestants with their total votes.
    1. Authorization header with Bearer token is required
    2. Field event_id is required
    3. Response code success must be 200 OK
    4. Response code failure for invalid fields must be 400 Bad Request
    5. Response code failure for unauthorized must be 401 Unauthorized
    6. Response code failure event_id not found must be 404 Not Found
    - headers
  ```json
  {
    Authorization : Bearer "string"
  }
  ```
    - response
  ```json
  {
    "event_id" : "string",
    "event_name" : "string",
    "contestants": [
      {
        "contestant_id": "string",
        "contestant_name": "string",
        "total_votes": Integer
      }
    ]
  }
  ```

### <span style='color:#3BC143 ;font-weight: bold;'>GET CONTESTANTS LIST</span>
method: <span style='color:#FFBE33;font-weight: bold;'>GET</span>
path: <span style='color:#FFBE33;font-weight: bold;'>v1/contestants/{event_id}</span>
- Endpoint to get the list of contestants for a given event. Response must return the event_id and the list of contestants.
    1. Authorization header with Bearer token is required
    2. Url parameter field event_id is required
    3. Response code success must be 200 OK
    4. Response code failure for invalid fields must be 400 Bad Request
    5. Response code failure for unauthorized must be 401 Unauthorized
    6. Response code failure for event_id not found must be 404 Not Found
    - headers
  ```json
  {
    Authorization : Bearer "string"
  }
  ```
    - response
  ```json
  {
    "event_id" : "string",
    "contestants": [
      {
        "contestant_id": "string",
        "contestant_name": "string",
        "contestant_image_url": "string"
      }
    ]
  }
  ```

### <span style='color:#3BC143 ;font-weight: bold;'>SUBMIT A VOTE</span>
method: <span style='color:#FFBE33;font-weight: bold;'>POST</span>
path: <span style='color:#FFBE33;font-weight: bold;'>v1/vote/submit</span>
- Endpoint to submit a vote for a given event. Response must return the vote_id.
    1. Authorization header with Bearer token is required
    2. Fields user_id, event_id, contestant_id are required
    3. Response code success must be 200 OK
    4. Response code failure for invalid fields must be 400 Bad Request
    5. Response code failure for unauthorized must be 401 Unauthorized
    6. Response code failure for contestant_id and/or event_id not found must be 404 Not Found
    - headers
  ```json
  {
    Authorization : Bearer "string"
  }
  ```
    - request
  ```json
  {
    "user_id": "string",
    "event_id": "string",
    "contestant_id": "string",
    "client_timestamp": "string",
    "meta": {
        "device_id": "string",
        "app_version": "string"
    }
  }
  ```
    - response
  ```json
  {
    vote_id : "string"
  }
  ```

### <span style='color:#3BC143 ;font-weight: bold;'>VERIFY VOTE STATUS</span>
method: <span style='color:#FFBE33;font-weight: bold;'>GET</span>
path: <span style='color:#FFBE33;font-weight: bold;'>v1/vote/{vote_id}/status</span>
- Endpoint to verify the status of a submitted vote. Response must return the vote_id and its current status ("acked|processed|duplicate|rejected") and the event_name.
    1. Authorization header with Bearer token is required
    2. Fields vote_id is required
    3. Response code success must be 200 OK
    4. Response code failure for invalid fields must be 400 Bad Request
    5. Response code failure for unauthorized must be 401 Unauthorized
    6. Response code failure for vote_id not found must be 404 Not Found
    - headers
  ```json
  {
    Authorization : Bearer "string"
  }
  ```
    - response
  ```json
  {
    vote_status : "string",
    event_name : "string"
  }
  ```