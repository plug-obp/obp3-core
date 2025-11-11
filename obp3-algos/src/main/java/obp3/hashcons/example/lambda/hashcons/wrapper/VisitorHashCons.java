package obp3.hashcons.example.lambda.hashcons.wrapper;

import obp3.hashcons.example.lambda.syntax.Visitor;

public interface VisitorHashCons<I,O> extends Visitor<I,O> {
    O accept(TermHashConsed node, I input);
}
