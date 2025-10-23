package obp3.sli.core.operators.product;

import java.util.Optional;

public record Step<LA, LC>(LC start, Optional<LA> action, LC end) { }
