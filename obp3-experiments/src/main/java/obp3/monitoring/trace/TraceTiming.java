package obp3.monitoring.trace;

import java.time.Instant;

public record TraceTiming(Instant lastInstant, Instant currentInstant) {
}
