package obp3.monitoring.trace;

import java.time.Instant;

public record TraceConfiguration<M>(M measurement, Instant instant) {
}
