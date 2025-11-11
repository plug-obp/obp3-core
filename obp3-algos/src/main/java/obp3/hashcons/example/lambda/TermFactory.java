package obp3.hashcons.example.lambda;

import obp3.hashcons.example.lambda.syntax.App;
import obp3.hashcons.example.lambda.syntax.Lambda;
import obp3.hashcons.example.lambda.syntax.Term;
import obp3.hashcons.example.lambda.syntax.Var;

public class TermFactory {

    public Term var(int index) {
        return new Var(index);
    }

    public Term app(Term func, Term arg) {
        return new App(func, arg);
    }

    public Term lambda(Term body) {
        return new Lambda(body);
    }
}
