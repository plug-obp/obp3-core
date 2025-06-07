package obp3.scc;

public class TarjanVertexData<V> {
    public int low = 0;
    public boolean lead = false;
    public V ptr;

    public static TarjanVertexData DEFAULT = new TarjanVertexData<>();
}
