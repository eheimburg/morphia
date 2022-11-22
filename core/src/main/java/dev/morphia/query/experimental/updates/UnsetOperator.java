package dev.morphia.query.experimental.updates;

import dev.morphia.internal.PathTarget;
import dev.morphia.mapping.Mapper;
import dev.morphia.query.OperationTarget;

import org.bson.Document;

/**
 * @morphia.internal
 * @since 2.0
 */
public class UnsetOperator extends UpdateOperator {
    /**
     * @param field the field
     * @morphia.internal
     */
    public UnsetOperator(String field) {
        super("$unset", field, "unused");
    }

    @Override
    public OperationTarget toTarget(PathTarget pathTarget) {
        return new OperationTarget(pathTarget, "") {
            @Override
            public Object encode(Mapper mapper) {
                return new Document(pathTarget.translatedPath(), "");
            }
        };
    }
}
