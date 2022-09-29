package dev.morphia.query;

import com.mongodb.ServerAddress;
import com.mongodb.ServerCursor;
import com.mongodb.client.MongoCursor;
import com.mongodb.lang.NonNull;
import dev.morphia.internal.EntityCache;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <T> the original type being iterated
 * @since 2.2
 */
public class MorphiaCursor<T> implements MongoCursor<T> {
    private final MongoCursor<T> wrapped;

    private final EntityCache cache;

    /**
     * Creates a MorphiaCursor
     *
     * @param cursor the Iterator to use
     * @param cache
     */
    public MorphiaCursor(MongoCursor<T> cursor, EntityCache cache) {
        wrapped = cursor;
        this.cache = cache;
        cache.close();
    }

    /**
     * Closes the underlying cursor.
     */
    public void close() {
        wrapped.close();
    }

    @Override
    public boolean hasNext() {
        return wrapped.hasNext();
    }

    @Override
    @NonNull
    public T next() {
        try (AutoCloseable cache = configureCache()) {
            return wrapped.next();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private AutoCloseable configureCache() {
        EntityCache.set(cache);
        return cache;
    }

    @Override
    public int available() {
        return wrapped.available();
    }

    @Override
    public T tryNext() {
        try (AutoCloseable cache = configureCache()) {
            return wrapped.tryNext();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public ServerCursor getServerCursor() {
        return wrapped.getServerCursor();
    }

    @Override
    @NonNull
    public ServerAddress getServerAddress() {
        return wrapped.getServerAddress();
    }

    @Override
    public void remove() {
        wrapped.remove();
    }

    /**
     * Converts this cursor to a List. Care should be taken on large datasets as OutOfMemoryErrors are a risk.
     *
     * @return the list of Entities
     */
    public List<T> toList() {
        final List<T> results = new ArrayList<>();
        try (wrapped) {
            while (wrapped.hasNext()) {
                results.add(next());
            }
        }
        return results;
    }

}
