package obp3.hashcons.example.lambda.hashcons.expando;

import obp3.hashcons.HashConsTable;
import obp3.hashcons.HashConsed;
import obp3.hashcons.Hashable;
import obp3.hashcons.example.lambda.TermFactory;
import obp3.hashcons.example.lambda.syntax.App;
import obp3.hashcons.example.lambda.syntax.Lambda;
import obp3.hashcons.example.lambda.syntax.Term;
import obp3.hashcons.example.lambda.syntax.Var;
import obp3.sli.core.operators.product.Product;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class ExpandoTermHashConsedFactory extends TermFactory {
    private final HashConsTable<Term> table;
    private final Map<Term, Product<Integer, Integer>> hashConsMap = Collections.synchronizedMap(new WeakHashMap<>());

    HashConsed<Term> associate(Term node, int tag, int hashKey) {
        var data = new Product<>(tag, hashKey);
        hashConsMap.put(node, data);
        return new HashConsed.FunctionalHashConsed<>(
                () -> node,
                data::l,
                data::r
        );
    }

    public ExpandoTermHashConsedFactory() {
        this.table = new HashConsTable<>(new Hashable<>(){}, this::associate);
    }
    public ExpandoTermHashConsedFactory(HashConsTable<Term> table) {
        this.table = table;
    }

    public Map<Term, Product<Integer, Integer>> getHashConsMap() {
        return hashConsMap;
    }

    public Term hashConsed(Term term) {
        return table.hashCons(term).node();
    }

    @Override
    public Term var(int index) {
        return hashConsed(new Var(index));
    }

    @Override
    public Term app(Term func, Term arg) {
        return hashConsed(new App(hashConsed(func), hashConsed(arg)));
    }

    @Override
    public Term lambda(Term body) {
        return hashConsed(new Lambda(hashConsed(body)));
    }
}
