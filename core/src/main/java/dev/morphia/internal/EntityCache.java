package dev.morphia.internal;

import com.mongodb.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

public class EntityCache implements AutoCloseable {
    private static final ThreadLocal<EntityCache> reference = new ThreadLocal<>();

    private final Map<Object, Object> cache;
    private final boolean secondary;

    private EntityCache() {
        EntityCache current = reference.get();
        secondary = current != null;
        if (secondary) {
            cache = current.cache;
        } else {
            cache = new HashMap<>();
            EntityCache.set(this);
        }
    }

    @Override
    public void close() {
        if (!secondary) {
            reference.remove();
        }
    }

    public static EntityCache get() {
        EntityCache entityCache = reference.get();
        return entityCache != null ? entityCache : new EntityCache();
    }

    public static void set(EntityCache cache) {
        reference.set(cache);
    }

    @Nullable
    public Object get(Object idValue) {
        return cache.get(idValue);
    }
}
