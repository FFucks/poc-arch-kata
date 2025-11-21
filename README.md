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

# Endpoints:

Authentication:

POST /v1/auth/token

body:

{
    "username": "user-name-123",
    "password": "123"
}

RETURN payload:
{
    "access_token": "SHA256-base64-token",
}

Registration:

headers:
{
    "Authorization": Bearer <token>
}

POST /v1/auth/register

body: 
{
    "username": "user-name-123",
    "email": "user-email",
    "date_of_birth": "1988-01-01"
}

Submit a vote:

URI: POST /v1/vote

body:
{
    "vote_id": "uuid-v4",
    "user_id": "user-123",
    "event_id": "uuid-event",
    "client_timestamp": "2025-11-18T07:00:00Z",
    "client_sig": "base64(...)",
    "meta": { 
        "device_id":"abc",
        "app_version":"1.2.3" 
    }
}

Leaderboard - Realtime:

GET /v1/event/{event_id}/leaderboard

RETURN payload:
{
    "event_id":"big-brother-season-2025",
    "timestamp":"2025-11-18T07:03:12Z",
    "top":[
        {
            "option_id":"contestant-1",
            "total":123456
        },
        {
            "option_id":"contestant-2",
            "total":86754
        },
    ]
}


Verify the vote status:

GET /v1/vote/status/{vote_id}

RETURN payload
{ 
    "user_id": "user-123",
    "vote_id": "uuid-vote", 
    "status": "acked|processed|duplicate|rejected",
}


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