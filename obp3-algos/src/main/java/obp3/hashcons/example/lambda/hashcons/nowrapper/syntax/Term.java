package obp3.hashcons.example.lambda.hashcons.nowrapper.syntax;

import obp3.hashcons.HashConsed;

public interface Term extends HashConsed<Term> {
    default <I, O> O accept(Visitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }
    HashConsed<Term> toHashCons(int tag, int hashKey);
}

