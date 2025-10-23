package obp3.sli.core.operators.product.deterministic.model;

import obp3.runtime.sli.DeterministicSemanticRelation;
import obp3.runtime.sli.IDeterministicSematicRelation;
import obp3.runtime.sli.Step;

public record DeterministicStepProductParameters<LA, LC, RA, RC>(
        DeterministicSemanticRelation<LA, LC> lhs,
        IDeterministicSematicRelation<Step<LA, LC>, RA, RC> rhs
) {}
