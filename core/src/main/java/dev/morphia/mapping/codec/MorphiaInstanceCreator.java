package dev.morphia.mapping.codec;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import dev.morphia.annotations.internal.MorphiaInternal;
import dev.morphia.internal.EntityCache;
import dev.morphia.mapping.codec.pojo.PropertyModel;

/**
 * Marker interface for creators
 *
 * @morphia.internal
 */
@MorphiaInternal
public interface MorphiaInstanceCreator {
    /**
     * @return the new class instance.
     */
    Object getInstance();

    /**
     * Sets the entity found in the cache
     *
     * @param entity the cached entity
     * @since 2.3
     * @see EntityCache
     */
    void setInstance(Object entity);

    /**
     * Sets a value for the given FieldModel
     *
     * @param value the value
     * @param model the model
     */
    void set(@Nullable Object value, @NonNull PropertyModel model);
}
