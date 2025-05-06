package obp3.fixer.exemple.cfl.model;

public interface FunctionalVisitor<I, O> {
    O visit(Language node, I input);
    O visit(Terminal node, I input);
    O visit(Composite node, I input);
    O visit(Empty node, I input);
    O visit(Epsilon node, I input);
    <T> O visit(Token<T> node, I input);
    O visit(Sum node, I input);
    O visit(Product node, I input);
    O visit(Delta node, I input);
    O visit(Reference node, I input);

    class Base<I, O> implements FunctionalVisitor<I, O> {
        @Override
        public O visit(Language node, I input) {
            throw new UnsupportedOperationException();
        }

        @Override
        public O visit(Terminal node, I input) {
            return ((Language)node).accept(this, input);
        }

        @Override
        public O visit(Composite node, I input) {
            return ((Language)node).accept(this, input);
        }

        @Override
        public O visit(Empty node, I input) {
            return ((Terminal)node).accept(this, input);
        }

        @Override
        public O visit(Epsilon node, I input) {
            return ((Terminal)node).accept(this, input);
        }

        @Override
        public <T> O visit(Token<T> node, I input) {
            return ((Terminal)node).accept(this, input);
        }

        @Override
        public O visit(Sum node, I input) {
            return ((Composite)node).accept(this, input);
        }

        @Override
        public O visit(Product node, I input) {
            return ((Composite)node).accept(this, input);
        }

        @Override
        public O visit(Delta node, I input) {
            return ((Composite)node).accept(this, input);
        }

        @Override
        public O visit(Reference node, I input) {
            return ((Composite)node).accept(this, input);
        }
    }
}
