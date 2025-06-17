package obp3.buchi.ndfs;

import java.util.ArrayList;
import java.util.List;

public class EmptinessCheckerAnswer<V> {
    public boolean holds = true;
    public V witness;
    public List<V> trace = new ArrayList<>();

    public EmptinessCheckerAnswer() {}
}
