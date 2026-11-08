package org.maia.autorizador.shared.observability;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.maia.autorizador.support.IntegrationTestBase;

/** Guards the wiring every log line relies on: an active span puts traceId into the MDC. */
class TracingCorrelationTests extends IntegrationTestBase {
    @Autowired
    Tracer tracer;

    @Test
    void activeSpanExposesTraceIdInMdc() {
        var span = tracer.nextSpan().name("correlation-check").start();
        try (var ignored = tracer.withSpan(span)) {
            assertNotNull(MDC.get("traceId"));
        } finally {
            span.end();
        }
        assertNull(MDC.get("traceId"));
    }
}
