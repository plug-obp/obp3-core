package obp3.sli.core.operators.product.model;

import obp3.sli.core.ISemanticRelation;
import obp3.sli.core.SemanticRelation;
import obp3.sli.core.operators.product.Step;

public record StepProductParameters<LA, LC, RA, RC>(
        SemanticRelation<LA, LC> lhs,
        ISemanticRelation<Step<LA, LC>, RA, RC> rhs
){}

