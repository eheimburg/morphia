/*
 * Copyright (c) 2008 - 2013 MongoDB, Inc. <http://mongodb.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.morphia.aggregation;

import dev.morphia.geo.Point;
import dev.morphia.query.Query;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Outputs documents in order of nearest to farthest from a specified point.
 *
 * @mongodb.driver.manual reference/operator/aggregation/geoNear/ geoNear
 */
@SuppressWarnings("removal")
@Deprecated(since = "2.0", forRemoval = true)
public final class GeoNear {
    private final double[] nearLegacy;
    private final com.mongodb.client.model.geojson.Geometry nearGeoJson;
    private final String distanceField;
    private final Long limit;
    private final Long maxDocuments;
    private final Double maxDistance;
    private final Query query;
    private final Boolean spherical;
    private final Double distanceMultiplier;
    private final String includeLocations;

    private GeoNear(GeoNearBuilder builder) {
        nearLegacy = builder.nearLegacy;
        nearGeoJson = builder.nearGeoJson;
        distanceField = builder.distanceField;
        limit = builder.limit;
        maxDocuments = builder.maxDocuments;
        maxDistance = builder.maxDistance;
        query = builder.query;
        spherical = builder.spherical;
        distanceMultiplier = builder.distanceMultiplier;
        includeLocations = builder.includeLocations;
    }

    /**
     * Creates a builder for a GeoNear pipeline stage
     *
     * @param distanceField the field to process
     * @return the GeoNearBuilder
     */
    public static GeoNearBuilder builder(String distanceField) {
        return new GeoNearBuilder(distanceField);
    }

    /**
     * @return the distance field used in this stage
     */
    public String getDistanceField() {
        return distanceField;
    }

    /**
     * @return the distance multiplier used in this stage
     */
    public Double getDistanceMultiplier() {
        return distanceMultiplier;
    }

    /**
     * This specifies the output field that identifies the location used to calculate the distance. This option is useful when a location
     * field contains multiple locations.
     *
     * @return the field
     */
    public String getIncludeLocations() {
        return includeLocations;
    }

    /**
     * @return the maximum number of documents to return
     */
    public Long getLimit() {
        return limit;
    }

    /**
     * The maximum distance from the center point that the documents can be. MongoDB limits the results to those documents that fall within
     * the specified distance from the center point.
     *
     * @return the maximum
     */
    public Double getMaxDistance() {
        return maxDistance;
    }

    /**
     * The num option provides the same function as the limit option. Both define the maximum number of documents to return. If both
     * options
     * are included, the num value overrides the limit value.
     *
     * @return the maximum
     */
    public Long getMaxDocuments() {
        return maxDocuments;
    }

    /**
     * The point for which to find the closest documents.
     * <p/>
     * If using a 2dsphere index, you can specify the point as either a GeoJSON point or legacy coordinate pair.
     * <p/>
     * If using a 2d index, specify the point as a legacy coordinate pair.
     *
     * @return the point
     */
    public double[] getNear() {
        double[] copy = new double[0];
        if (nearLegacy != null) {
            copy = new double[nearLegacy.length];
            System.arraycopy(nearLegacy, 0, copy, 0, nearLegacy.length);
        }
        return copy;
    }

    /**
     * Limits the results to the documents that match the query.
     *
     * @return the query
     */
    public Query getQuery() {
        return query;
    }

    /**
     * Required if using a 2dsphere index. Determines how MongoDB calculates the distance. The default value is false.
     * <p/>
     * If true, then MongoDB uses spherical geometry to calculate distances in meters if the specified (near) point is a GeoJSON point and
     * in radians if the specified (near) point is a legacy coordinate pair.
     * <p/>
     * If false, then MongoDB uses 2d planar geometry to calculate distance between points.
     * <p/>
     * If using a 2dsphere index, spherical must be true.
     *
     * @return true if using spherical geometry
     */
    public Boolean getSpherical() {
        return spherical;
    }

    /**
     * Provides a builder for GeoNear instances.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public static class GeoNearBuilder {
        private final String distanceField;
        private Long limit;
        private Long maxDocuments;
        private Double maxDistance;
        private Query query;
        private Boolean spherical;
        private Double distanceMultiplier;
        private String includeLocations;
        private double[] nearLegacy;
        private com.mongodb.client.model.geojson.Geometry nearGeoJson;

        /**
         * @param distanceField The output field that contains the calculated distance. To specify a field within a subdocument, use dot
         *                      notation.
         * @see <a href="http://docs.mongodb.org/master/reference/glossary/#term-dot-notation">dot notation</a>
         */
        public GeoNearBuilder(String distanceField) {
            this.distanceField = distanceField;
        }

        /**
         * @return the GeoNear instance as configured by this builder
         */
        public GeoNear build() {
            return new GeoNear(this);
        }

        /**
         * The factor to multiply all distances returned by the query. For example, use the distanceMultiplier to convert radians, as
         * returned by a spherical query, to kilometers by multiplying by the radius of the Earth.
         *
         * @param distanceMultiplier the distance multiplier used in this stage
         * @return this
         */
        public GeoNearBuilder setDistanceMultiplier(Double distanceMultiplier) {
            this.distanceMultiplier = distanceMultiplier;
            return this;
        }

        /**
         * This specifies the output field that identifies the location used to calculate the distance. This option is useful when a
         * location field contains multiple locations. To specify a field within a subdocument, use dot notation.
         *
         * @param includeLocations the output field that identifies the location used to calculate the distance
         * @return this
         * @see <a href="http://docs.mongodb.org/master/reference/glossary/#term-dot-notation">dot notation</a>
         */
        public GeoNearBuilder setIncludeLocations(String includeLocations) {
            this.includeLocations = includeLocations;
            return this;
        }

        /**
         * The maximum number of documents to return. The default value is 100.
         *
         * @param limit the maximum
         * @return this
         * @see #setMaxDocuments(Long).
         */
        public GeoNearBuilder setLimit(Long limit) {
            this.limit = limit;
            return this;
        }

        /**
         * A distance from the center point. Specify the distance in radians. MongoDB limits the results to those documents that fall
         * within
         * the specified distance from the center point.
         *
         * @param maxDistance the maximum
         * @return this
         */
        public GeoNearBuilder setMaxDistance(Double maxDistance) {
            this.maxDistance = maxDistance;
            return this;
        }

        /**
         * The maxDocuments option provides the same function as the limit option. Both define the maximum number of documents to return.
         * If
         * both options are included, this value overrides the limit value.
         *
         * @param num the maximum
         * @return this
         */
        public GeoNearBuilder setMaxDocuments(Long num) {
            this.maxDocuments = num;
            return this;
        }

        /**
         * Sets the point for which to find the closest documents.
         *
         * @param latitude  the latitude
         * @param longitude the longitude
         * @return this
         */
        public GeoNearBuilder setNear(double latitude, double longitude) {
            this.nearLegacy = new double[] { longitude, latitude };
            return this;
        }

        /**
         * Sets the point for which to find the closest documents.
         *
         * @param point a GeoJSON single point location.
         * @return this
         * @deprecated use the driver-provided types instead
         */
        @Deprecated(since = "2.0", forRemoval = true)
        public GeoNearBuilder setNear(Point point) {
            return setNear(point.convert());
        }

        /**
         * Sets the point for which to find the closest documents.
         *
         * @param point a GeoJSON single point location.
         * @return this
         */
        public GeoNearBuilder setNear(com.mongodb.client.model.geojson.Point point) {
            this.nearGeoJson = point;
            return this;
        }

        /**
         * Limits the results to the documents that match the query. The query syntax is the usual MongoDB read operation query syntax.
         *
         * @param query the query used to limit the documents to consider
         * @return this
         * @mongodb.driver.manual tutorial/query-documents/ read operation query syntax
         */
        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public GeoNearBuilder setQuery(Query query) {
            this.query = query;
            return this;
        }

        /**
         * If true, MongoDB references points using a spherical surface. The default value is false.
         *
         * @param spherical true if spherical geometry should be used
         * @return this
         */
        public GeoNearBuilder setSpherical(Boolean spherical) {
            this.spherical = spherical;
            return this;
        }
    }
}
