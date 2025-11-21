package com.ffucks.controller;

import com.ffucks.config.OpenTelemetryConfig;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.UUID;

@RestController
@RequestMapping("/v1")
public class VoteController {
    private final Tracer tracer = OpenTelemetryConfig.getTracer();


    @PostMapping("/vote")
    public ResponseEntity<?> vote(@RequestBody Map<String,Object> body) {
        String voteId = (String) body.getOrDefault("vote_id", UUID.toString());
        String userId = (String) body.getOrDefault("user_id", "anonymous");
        String eventId = (String) body.getOrDefault("event_id", "event-1");
        String optionId = (String) body.getOrDefault("option_id", "opt-1");

        Span span = tracer.spanBuilder("api.handle_vote")
                .setAttribute("vote_id", voteId)
                .setAttribute("user_id", userId)
                .setAttribute("event_id", eventId)
                .setAttribute("option_id", optionId)
                .startSpan();
        try (var scope = span.makeCurrent()) {
            Span v = tracer.spanBuilder("validate.user").startSpan();
            try (var s2 = v.makeCurrent()) {
            } finally {
                v.end();
            }

            Span produce = tracer.spanBuilder("commitlog.produce").startSpan();
            try (var s3 = produce.makeCurrent()) {
                Thread.sleep(5);
            } finally {
                produce.end();
            }

            return ResponseEntity.accepted().body(Map.of("status", "queued", "vote_id", voteId));
        } catch (InterruptedException e) {
            span.recordException(e);
            return ResponseEntity.status(500).body("error");
        } finally {
            span.end();
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> leaderboard() {
        return ResponseEntity.ok(Map.of("event_id", "event-1", "top", Map.of("opt-1", 100)));
    }
}
