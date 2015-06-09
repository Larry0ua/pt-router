package dospring.processor.matrix;

public class Pair<K, V> {
    public K k;
    public V v;
    public Pair(K key, V value) {
        k = key;
        v = value;
    }
    static<K,V> Pair<K,V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    public K k() {
        return k;
    }
    public V v() {
        return v;
    }
}
