package dev.morphia.query.experimental.filters;

import com.mongodb.client.model.geojson.Point;
import dev.morphia.mapping.Mapper;
import org.bson.BsonWriter;
import org.bson.codecs.EncoderContext;

class Box extends Filter {

    private final Point bottomLeft;
    private final Point upperRight;

    protected Box(final String field, final Point bottomLeft, final Point upperRight) {
        super("$box", field, null);
        this.bottomLeft = bottomLeft;
        this.upperRight = upperRight;
    }

    @Override
    public void encode(final Mapper mapper, final BsonWriter writer, final EncoderContext context) {
        writer.writeStartDocument(field(mapper));
        writer.writeStartDocument("$geoWithin");

        writer.writeStartArray(getFilterName());
        writer.writeStartArray();
        for (final Double value : bottomLeft.getPosition().getValues()) {
            writer.writeDouble(value);
        }
        writer.writeEndArray();
        writer.writeStartArray();
        for (final Double value : upperRight.getPosition().getValues()) {
            writer.writeDouble(value);
        }
        writer.writeEndArray();
        writer.writeEndArray();

        writer.writeEndDocument();
        writer.writeEndDocument();
    }
}
