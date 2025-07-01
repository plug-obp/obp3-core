package obp3.sli.core.operators.product;

public record Product<L, R>(L l, R r) {
    @Override
    public String toString() {
        return "⟨" + "l=" + l + ", r=" + r + '⟩';
    }
}
