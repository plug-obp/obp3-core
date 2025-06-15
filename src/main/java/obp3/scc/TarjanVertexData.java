package obp3.scc;

public class TarjanVertexData<V> {
    public int low = 0;
    public boolean lead = false;
    public V ptr;

    public static <X> TarjanVertexData<X> DEFAULT() { return new TarjanVertexData<>(); }

    @Override
    public String toString() {
        return "VData["+ low + ", " + lead + ", " + ptr + "]";
    }
}
