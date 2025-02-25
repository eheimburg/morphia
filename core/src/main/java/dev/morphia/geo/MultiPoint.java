package dev.morphia.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mongodb.lang.Nullable;

import dev.morphia.annotations.Id;

import org.bson.types.ObjectId;

/**
 * This class represents a series of points, which will saved into MongoDB as per the <a href="http://geojson.org/geojson-spec
 * .html#id5">GeoJSON specification</a>.
 * <p/>
 * The factory for creating a MultiPoint is the {@code GeoJson.multiPoint} method.
 *
 * @see dev.morphia.geo.GeoJson#multiPoint(Point...)
 * @deprecated use the driver-provided types instead
 */
@SuppressWarnings("removal")
@Deprecated(since = "2.0", forRemoval = true)
public class MultiPoint implements Geometry {
    @Id
    private ObjectId id;
    private final List<Point> coordinates;

    @SuppressWarnings("UnusedDeclaration") // used by Morphia
    private MultiPoint() {
        this.coordinates = new ArrayList<Point>();
    }

    MultiPoint(Point... points) {
        this.coordinates = Arrays.asList(points);
    }

    MultiPoint(List<Point> coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public List<Point> getCoordinates() {
        return coordinates;
    }

    @Override
    public int hashCode() {
        return coordinates.hashCode();
    }

    /* equals, hashCode and toString. Useful primarily for testing and debugging. Don't forget to re-create when changing this class */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MultiPoint that = (MultiPoint) o;

        return coordinates.equals(that.coordinates);
    }

    @Override
    public String toString() {
        return "MultiPoint{"
                + "coordinates=" + coordinates
                + '}';
    }

    @Override
    public com.mongodb.client.model.geojson.MultiPoint convert() {
        return convert(null);
    }

    @Override
    public com.mongodb.client.model.geojson.MultiPoint convert(@Nullable CoordinateReferenceSystem crs) {
        return new com.mongodb.client.model.geojson.MultiPoint(crs != null ? crs.convert() : null,
                GeoJson.convertPoints(coordinates));
    }
}
