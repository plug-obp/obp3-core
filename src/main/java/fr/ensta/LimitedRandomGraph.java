package fr.ensta;

import fr.ensta.obp3.RootedGraph;

import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

public class LimitedRandomGraph implements RootedGraph<Long> {
    long limit;
    int width;
    long seed;
    Random random;
    public LimitedRandomGraph(long limit, int width, long seed) {
        this.limit = limit;
        this.width = width;
        this.seed = seed;
        this.random = new Random(seed);
    }
    @Override
    public Iterator<Long> roots() {
        return random.longs(random.nextInt(width) + 1).iterator();
    }

    @Override
    public Iterator<Long> neighbours(Long o) {
        if (limit == 0) return Collections.emptyIterator();
        limit--;
        return random.longs(random.nextInt(width)).iterator();
    }
}