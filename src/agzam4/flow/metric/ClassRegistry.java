package agzam4.flow.metric;

import arc.func.Cons;
import arc.struct.ObjectMap;

public class ClassRegistry<V> {

	public boolean iterfaces; 
    private final ObjectMap<Class<?>, V> registry = new ObjectMap<>();
    private final ObjectMap<Class<?>, V> cache = new ObjectMap<>();

    public void register(Class<?> c, V v) {
        if (c == null) return;
        registry.put(c, v);
        cache.clear();
    }

    public boolean registrated(Class<?> c) {
        return registry.containsKey(c);
    }

    public V get(Class<?> c) {
        if(c == null) return null;
        if(cache.containsKey(c)) return cache.get(c);
        V value = findNearest(c);
        cache.put(c, value);
        return value;
    }

    private V findNearest(Class<?> c) {
        if(registry.containsKey(c)) return registry.get(c);

        if (iterfaces) {
            for (Class<?> iface : c.getInterfaces()) {
                V value = findNearest(iface);
                if (value != null) {
                    return value;
                }
            }
        }

        Class<?> superClass = c.getSuperclass();
        if(superClass != null) return findNearest(superClass);
        return null;
    }

    public void each(Cons<V> vs) {
    	for (var v : registry.values()) {
    		vs.get(v);
    	}
    }
}
