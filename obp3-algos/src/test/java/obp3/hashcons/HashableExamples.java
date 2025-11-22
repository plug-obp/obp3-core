package obp3.hashcons;

import obp3.datastructures.collections.linearscan.LinearScanHashSet;
import obp3.datastructures.collections.linearscan.LinearScanHashMap;
import obp3.utils.Hashable;

/**
 * Examples demonstrating the improved Hashable interface and its usage.
 */
public class HashableExamples {
    
    /**
     * Example 1: Using standard hashable (default behavior)
     */
    public static void exampleStandardHashable() {
        // Using the factory method
        var set = new LinearScanHashSet<String>(16, Hashable.standard());
        
        // Or using the default constructor (which uses Hashable.standard() internally)
        // var set = new LinearScanHashSet<String>(16);
        
        set.add("hello");
        set.add("world");
        
        System.out.println("Standard hashable set size: " + set.size());
    }
    
    /**
     * Example 2: Using identity hashable (reference equality)
     */
    public static void exampleIdentityHashable() {
        var set = new LinearScanHashSet<String>(16, Hashable.identity());
        
        String s1 = new String("test");
        String s2 = new String("test");
        String s3 = s1;
        
        set.add(s1);
        set.add(s2); // Different object, so added even though equal by value
        set.add(s3); // Same object as s1, not added
        
        System.out.println("Identity hashable set size: " + set.size()); // Should be 2
    }
    
    /**
     * Example 3: Using comparator-based hashable
     */
    public static void exampleComparatorHashable() {
        // Case-insensitive string comparison
        var hashable = Hashable.fromComparator(String.CASE_INSENSITIVE_ORDER);
        var set = new LinearScanHashSet<>(16, hashable);
        
        set.add("Hello");
        set.add("HELLO"); // Considered equal due to case-insensitive comparison
        set.add("World");
        
        System.out.println("Case-insensitive set size: " + set.size()); // Should be 2
    }
    
    /**
     * Example 4: Custom hashable with validation
     */
    public static void exampleCustomHashable() {
        record Point(int x, int y) {}
        
        // Custom hashable that only considers x coordinate
        Hashable<Point> hashable = new Hashable<Point>() {
            @Override
            public int hash(Point p) {
                return p.x();
            }
            
            @Override
            public boolean equal(Point p1, Point p2) {
                if (p1 == null && p2 == null) return true;
                if (p1 == null || p2 == null) return false;
                return p1.x() == p2.x();
            }
        };
        
        var set = new LinearScanHashSet<>(16, hashable);
        
        // Validate the hashable contract
        Point p = new Point(5, 10);
        hashable.validateContract(p);
        
        set.add(new Point(5, 10));
        set.add(new Point(5, 20)); // Same x, so considered equal
        set.add(new Point(6, 10)); // Different x, so different
        
        System.out.println("Custom hashable set size: " + set.size()); // Should be 2
    }
    
    /**
     * Example 5: Using withPositiveHash() for safer hash codes
     */
    public static void examplePositiveHash() {
        // Custom hashable that might produce negative hashes
        Hashable<Integer> hashable = x -> x * 31; // Could be negative
        
        // Wrap it to ensure positive hashes
        var safeHashable = hashable.withPositiveHash();
        
        var set = new LinearScanHashSet<>(16, safeHashable);
        set.add(-100);
        set.add(100);
        
        System.out.println("Positive hash set size: " + set.size());
    }
    
    /**
     * Example 6: Using with LinearScanHashMap
     */
    public static void exampleWithHashMap() {
        record CaseInsensitiveKey(String value) {}
        
        Hashable<CaseInsensitiveKey> hashable = new Hashable<CaseInsensitiveKey>() {
            @Override
            public int hash(CaseInsensitiveKey key) {
                return key.value().toLowerCase().hashCode();
            }
            
            @Override
            public boolean equal(CaseInsensitiveKey k1, CaseInsensitiveKey k2) {
                if (k1 == null && k2 == null) return true;
                if (k1 == null || k2 == null) return false;
                return k1.value().equalsIgnoreCase(k2.value());
            }
        };
        
        var map = new LinearScanHashMap<CaseInsensitiveKey, Integer>(16, hashable);
        
        map.put(new CaseInsensitiveKey("Hello"), 1);
        map.put(new CaseInsensitiveKey("HELLO"), 2); // Updates the value
        map.put(new CaseInsensitiveKey("World"), 3);
        
        System.out.println("HashMap size: " + map.size()); // Should be 2
        System.out.println("Value for 'hello': " + map.get(new CaseInsensitiveKey("hello"))); // Should be 2
    }
    
    public static void main(String[] args) {
        System.out.println("=== Hashable Examples ===\n");
        
        System.out.println("Example 1: Standard Hashable");
        exampleStandardHashable();
        System.out.println();
        
        System.out.println("Example 2: Identity Hashable");
        exampleIdentityHashable();
        System.out.println();
        
        System.out.println("Example 3: Comparator Hashable");
        exampleComparatorHashable();
        System.out.println();
        
        System.out.println("Example 4: Custom Hashable with Validation");
        exampleCustomHashable();
        System.out.println();
        
        System.out.println("Example 5: Positive Hash");
        examplePositiveHash();
        System.out.println();
        
        System.out.println("Example 6: HashMap with Custom Hashable");
        exampleWithHashMap();
    }
}
