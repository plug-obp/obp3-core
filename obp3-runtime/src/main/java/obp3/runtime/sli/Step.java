package obp3.runtime.sli;

import java.util.Optional;

public record Step<LA, LC>(LC start, Optional<LA> action, LC end) { }
