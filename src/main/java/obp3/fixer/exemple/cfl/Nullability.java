package obp3.fixer.exemple.cfl;

import obp3.fixer.Fixer;
import obp3.fixer.Lattice;
import obp3.fixer.exemple.cfl.model.*;

import java.util.function.Function;

public class Nullability implements Function<Language, Boolean> {
    final IsNullable isNullableVisitor = new IsNullable();
    final Fixer<Language, Boolean> fixer = new Fixer<>(
            (Language n, Function<Language, Boolean> isNullable) -> n.accept(isNullableVisitor, isNullable),
            Lattice.BooleanLattice);

    @Override
    public Boolean apply(Language language) {
//        final var isNullableVisitor = new IsNullable();
//        Fixer<Language, Boolean> fix = new Fixer<>(
//                (Language n, Function<Language, Boolean> isNullable) -> n.accept(isNullableVisitor, isNullable),
//                Lattice.BooleanLattice);
        return fixer.apply(language);
    }

    static class IsNullable extends FunctionalVisitor.Base<Function<Language, Boolean>, Boolean> {
        @Override
        public Boolean visit(Empty node, Function<Language, Boolean> isNullable) {
            return false;
        }

        @Override
        public Boolean visit(Epsilon node, Function<Language, Boolean> isNullable) {
            return true;
        }

        @Override
        public <T> Boolean visit(Token<T> node, Function<Language, Boolean> isNullable) {
            return false;
        }

        @Override
        public Boolean visit(Sum node, Function<Language, Boolean> isNullable) {
            return isNullable.apply(node.lhs) || isNullable.apply(node.rhs);
        }

        @Override
        public Boolean visit(Product node, Function<Language, Boolean> isNullable) {
            return isNullable.apply(node.lhs) && isNullable.apply(node.rhs);
        }

        @Override
        public Boolean visit(Delta node, Function<Language, Boolean> isNullable) {
            return isNullable.apply(node.operand);
        }

        @Override
        public Boolean visit(Reference node, Function<Language, Boolean> isNullable) {
            return isNullable.apply(node.target);
        }
    }
}
