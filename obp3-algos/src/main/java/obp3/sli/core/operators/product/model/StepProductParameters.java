package obp3.sli.core.operators.product.model;

import obp3.runtime.sli.ISemanticRelation;
import obp3.runtime.sli.SemanticRelation;
import obp3.runtime.sli.Step;

public record StepProductParameters<LA, LC, RA, RC>(
        SemanticRelation<LA, LC> lhs,
        ISemanticRelation<Step<LA, LC>, RA, RC> rhs
){}

