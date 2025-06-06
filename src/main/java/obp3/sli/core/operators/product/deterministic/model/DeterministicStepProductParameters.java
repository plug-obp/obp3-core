package obp3.sli.core.operators.product.deterministic.model;

import obp3.sli.core.DeterministicSemanticRelation;
import obp3.sli.core.IDeterministicSematicRelation;
import obp3.sli.core.operators.product.Step;

public record DeterministicStepProductParameters<LA, LC, RA, RC>(
        DeterministicSemanticRelation<LA, LC> lhs,
        IDeterministicSematicRelation<Step<LA, LC>, RA, RC> rhs
) {}
