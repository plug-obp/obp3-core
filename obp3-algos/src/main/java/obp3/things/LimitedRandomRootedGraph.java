package obp3.things;

import obp3.runtime.sli.IRootedGraph;

import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

public class LimitedRandomRootedGraph implements IRootedGraph<Long> {
    long limit;
    int width;
    long seed;
    Random random;
    long count;
    public LimitedRandomRootedGraph(long limit, int width, long seed) {
        this.limit = limit;
        this.width = width;
        this.seed = seed;
        this.random = new Random(seed);
        this.count = 0;
    }
    @Override
    public Iterator<Long> roots() {
        return random.longs(random.nextInt(width) + 1).iterator();
    }

    @Override
    public Iterator<Long> neighbours(Long o) {
        if (count == limit) return Collections.emptyIterator();
        count++;
        return random.longs(random.nextInt(width)).iterator();
    }

    @Override
    public String toString() {
        return "LimitedRandomGraph{" +
                "limit=" + limit +
                ", width=" + width +
                ", seed=" + seed +
                '}';
    }
}