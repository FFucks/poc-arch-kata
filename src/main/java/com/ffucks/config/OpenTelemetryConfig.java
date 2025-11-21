package com.ffucks.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;

import java.time.Duration;

public class OpenTelemetryConfig {

    private static final OpenTelemetry openTelemetry = initOpenTelemetry();
    private static final Tracer tracer = openTelemetry.getTracer("voting-api");

    private static OpenTelemetry initOpenTelemetry() {
        String endpoint = System.getenv().getOrDefault("OTEL_EXPORTER_OTLP_ENDPOINT", "http://otel-collector:4317");
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(endpoint)
                .setTimeout(Duration.ofSeconds(10))
                .build();

        Resource service = Resource.getDefault()
                .merge(Resource.create(io.opentelemetry.api.common.Attributes.of(
                        ResourceAttributes.SERVICE_NAME, System.getenv().getOrDefault("OTEL_SERVICE_NAME", "voting-api")
                )));

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setSampler(Sampler.parentBased(Sampler.alwaysOn()))
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .setResource(service)
                .build();

        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .buildAndRegisterGlobal();

        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));
        return openTelemetrySdk;
    }

    public static Tracer getTracer() {
        return tracer;
    }

    public static OpenTelemetry getOpenTelemetry() {
        return openTelemetry;
    }
}
