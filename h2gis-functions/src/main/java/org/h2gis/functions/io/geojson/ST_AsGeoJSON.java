/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.geojson;

import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.CoordinateUtils;
import org.locationtech.jts.geom.*;

/**
 * Transform a JTS geometry to a GeoJSON geometry representation.
 *
 * @author Erwan Bocher
 */
public class ST_AsGeoJSON extends DeterministicScalarFunction {

    static  int maxdecimaldigits = 9;
    public ST_AsGeoJSON() {
        addProperty(PROP_REMARKS, "Return the geometry as a Geometry Javascript Object Notation (GeoJSON 1.0) element.\n"
                + "2D and 3D Geometries are both supported.\n"
                + "GeoJSON only supports SFS 1.1 geometry types (POINT, LINESTRING, POLYGON and COLLECTION)"
                + "maxdecimaldigits argument may be used to reduce the maximum number of decimal places used in output (defaults to 9).");
    }

    @Override
    public String getJavaStaticMethod() {
        return "toGeojson";
    }

    /**
     * Convert the geometry to a GeoJSON representation.
     *
     * @param geom Geometry
     * @return geojson representation
     */
    public static String toGeojson(Geometry geom) {
        if(geom==null){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        toGeojsonGeometry(geom,maxdecimaldigits, sb);
        return sb.toString();
    }

     /**
     * Convert the geometry to a GeoJSON representation.
     *
     * @param geom input geometry
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     * @return geojson representation
     */
    public static String toGeojson(Geometry geom, int maxdecimaldigits) {
        if(geom==null){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        toGeojsonGeometry(geom, maxdecimaldigits,sb);
        return sb.toString();
    }
    
    
    /**
     * Transform a JTS geometry to a GeoJSON representation.
     *
     * @param geom input geometry
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     * @param sb buffer to store the geojson
     */
    public static void toGeojsonGeometry(Geometry geom, int maxdecimaldigits, StringBuilder sb) {
        if (geom instanceof Point) {
            toGeojsonPoint((Point) geom, maxdecimaldigits,sb);
        } else if (geom instanceof LineString) {
            toGeojsonLineString((LineString) geom, maxdecimaldigits, sb);
        } else if (geom instanceof Polygon) {
            toGeojsonPolygon((Polygon) geom,maxdecimaldigits, sb);
        } else if (geom instanceof MultiPoint) {
            toGeojsonMultiPoint((MultiPoint) geom, maxdecimaldigits,sb);
        } else if (geom instanceof MultiLineString) {
            toGeojsonMultiLineString((MultiLineString) geom, maxdecimaldigits,sb);
        } else if (geom instanceof MultiPolygon) {
            toGeojsonMultiPolygon((MultiPolygon) geom,maxdecimaldigits, sb);
        } else {
            toGeojsonGeometryCollection((GeometryCollection) geom, maxdecimaldigits,sb);
        }
    }

    /**
     * For type "Point", the "coordinates" member must be a single position.
     *
     * A position is the fundamental geometry construct. The "coordinates"
     * member of a geometry object is composed of one position (in the case of a
     * Point geometry), an array of positions (LineString or MultiPoint
     * geometries), an array of arrays of positions (Polygons,
     * MultiLineStrings), or a multidimensional array of positions
     * (MultiPolygon).
     *
     * A position is represented by an array of numbers. There must be at least
     * two elements, and may be more. The order of elements must follow x, y, z
     * order (easting, northing, altitude for coordinates in a projected
     * coordinate reference system, or longitude, latitude, altitude for
     * coordinates in a geographic coordinate reference system). Any number of
     * additional elements are allowed -- interpretation and meaning of
     * additional elements is beyond the scope of this specification.
     *
     * Syntax:
     *
     * { "type": "Point", "coordinates": [100.0, 0.0] }
     *
     * @param point input point
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     * @param sb buffer to store the geojson
     */
    public static void toGeojsonPoint(Point point, int maxdecimaldigits, StringBuilder sb) {
        Coordinate coord = point.getCoordinate();
        sb.append("{\"type\":\"Point\",\"coordinates\":[");
        sb.append(CoordinateUtils.round(coord.x, maxdecimaldigits)).append(",").append(CoordinateUtils.round(coord.y, maxdecimaldigits));
        if (!Double.isNaN(coord.z)) {
            sb.append(",").append(CoordinateUtils.round(coord.z, maxdecimaldigits));
        }
        if (!Double.isNaN(coord.getM())) {
            sb.append(",").append(CoordinateUtils.round(coord.getM(), maxdecimaldigits));
        }
        sb.append("]}");
    }

    /**
     * Coordinates of a MultiPoint are an array of positions.
     *
     * Syntax:
     *
     * { "type": "MultiPoint", "coordinates": [ [100.0, 0.0], [101.0, 1.0] ] }
     *
     * @param multiPoint input {@link MultiPoint}
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     * @param sb buffer to store the geojson
     */
    public static void toGeojsonMultiPoint(MultiPoint multiPoint,int maxdecimaldigits,  StringBuilder sb) {
        sb.append("{\"type\":\"MultiPoint\",\"coordinates\":");
        toGeojsonCoordinates(multiPoint.getCoordinates(), maxdecimaldigits, sb);
        sb.append("}");
    }

    /**
     * Coordinates of LineString are an array of positions.
     *
     * Syntax:
     *
     * { "type": "LineString", "coordinates": [ [100.0, 0.0], [101.0, 1.0] ] }
     *
     * @param lineString input {@link MultiLineString}
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     * @param sb buffer to store the geojson
     */
    public static void toGeojsonLineString(LineString lineString, int maxdecimaldigits, StringBuilder sb) {
        sb.append("{\"type\":\"LineString\",\"coordinates\":");
        toGeojsonCoordinates(lineString.getCoordinates(),maxdecimaldigits, sb);
        sb.append("}");
    }

    /**
     * Coordinates of a MultiLineString are an array of LineString coordinate
     * arrays.
     *
     * Syntax:
     *
     * { "type": "MultiLineString", "coordinates": [ [ [100.0, 0.0], [101.0,
     * 1.0] ], [ [102.0, 2.0], [103.0, 3.0] ] ] }
     *
     * @param multiLineString input {@link MultiLineString}
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     * @param sb buffer to store the geojson
     */
    public static void toGeojsonMultiLineString(MultiLineString multiLineString,int maxdecimaldigits,  StringBuilder sb) {
        sb.append("{\"type\":\"MultiLineString\",\"coordinates\":[");
        int size = multiLineString.getNumGeometries();
        for (int i = 0; i < size; i++) {
            toGeojsonCoordinates(multiLineString.getGeometryN(i).getCoordinates(),maxdecimaldigits, sb);
            if (i < size - 1) {
                sb.append(",");
            }
        }
        sb.append("]}");
    }

    /**
     * Coordinates of a Polygon are an array of LinearRing coordinate arrays.
     * The first element in the array represents the exterior ring. Any
     * subsequent elements represent interior rings (or holes).
     *
     * Syntax:
     *
     * No holes:
     *
     * { "type": "Polygon", "coordinates": [ [ [100.0, 0.0], [101.0, 0.0],
     * [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ] ] }
     *
     * With holes:
     *
     * { "type": "Polygon", "coordinates": [ [ [100.0, 0.0], [101.0, 0.0],
     * [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ], [ [100.2, 0.2], [100.8, 0.2],
     * [100.8, 0.8], [100.2, 0.8], [100.2, 0.2] ] ] }
     *
     *
     * @param polygon input {@link Polygon}
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     * @param sb buffer to store the geojson
     */
    public static void toGeojsonPolygon(Polygon polygon,int maxdecimaldigits,  StringBuilder sb) {
        sb.append("{\"type\":\"Polygon\",\"coordinates\":[");
        //Process exterior ring
        toGeojsonCoordinates(polygon.getExteriorRing().getCoordinates(),maxdecimaldigits, sb);
        //Process interior rings
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            sb.append(",");
            toGeojsonCoordinates(polygon.getInteriorRingN(i).getCoordinates(),maxdecimaldigits, sb);
        }
        sb.append("]}");
    }

    /**
     * Coordinates of a MultiPolygon are an array of Polygon coordinate arrays.
     *
     * Syntax:
     *
     * { "type": "MultiPolygon", "coordinates": [ [[[102.0, 2.0], [103.0, 2.0],
     * [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]], [[[100.0, 0.0], [101.0, 0.0],
     * [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]], [[100.2, 0.2], [100.8, 0.2],
     * [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]] ] }
     *
     * @param multiPolygon input {@link MultiPolygon}
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     * @param sb buffer to store the geojson
     */
    public static void toGeojsonMultiPolygon(MultiPolygon multiPolygon, int maxdecimaldigits, StringBuilder sb) {
        sb.append("{\"type\":\"MultiPolygon\",\"coordinates\":[");
        int size = multiPolygon.getNumGeometries();
        for (int i = 0; i < size; i++) {
            Polygon p = (Polygon) multiPolygon.getGeometryN(i);
            sb.append("[");
            //Process exterior ring
            toGeojsonCoordinates(p.getExteriorRing().getCoordinates(),maxdecimaldigits, sb);
            //Process interior rings
            int size_p = p.getNumInteriorRing();
            for (int j = 0; j < size_p; j++) {
                sb.append(",");
                toGeojsonCoordinates(p.getInteriorRingN(j).getCoordinates(), maxdecimaldigits,sb);
            }
            sb.append("]");
            if (i < size - 1) {
                sb.append(",");
            }
        }
        sb.append("]}");
    }

    /**
     * A GeoJSON object with type "GeometryCollection" is a geometry object
     * which represents a collection of geometry objects.
     *
     * A geometry collection must have a member with the name "geometries". The
     * value corresponding to "geometries"is an array. Each element in this
     * array is a GeoJSON geometry object.
     *
     * Syntax:
     *
     * { "type": "GeometryCollection", "geometries": [ { "type": "Point",
     * "coordinates": [100.0, 0.0] }, { "type": "LineString", "coordinates": [
     * [101.0, 0.0], [102.0, 1.0] ] } ] }
     *
     * @param geometryCollection input {@link GeometryCollection}
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     * @param sb buffer to store the geojson
     */
    public static void toGeojsonGeometryCollection(GeometryCollection geometryCollection,int maxdecimaldigits,  StringBuilder sb) {
        sb.append("{\"type\":\"GeometryCollection\",\"geometries\":[");
        int size = geometryCollection.getNumGeometries();
        for (int i = 0; i < size; i++) {
            Geometry geom = geometryCollection.getGeometryN(i);
            if (geom instanceof Point) {
                toGeojsonPoint((Point) geom, maxdecimaldigits, sb);
            } else if (geom instanceof LineString) {
                toGeojsonLineString((LineString) geom, maxdecimaldigits, sb);
            } else if (geom instanceof Polygon) {
                toGeojsonPolygon((Polygon) geom,maxdecimaldigits, sb);
            }
            if (i < size - 1) {
                sb.append(",");
            }
        }
        sb.append("]}");
    }

    /**
     * Convert a jts array of coordinates to a GeoJSON coordinates
     * representation.
     *
     * Syntax:
     *
     * [[X1,Y1],[X2,Y2]]
     *
     * @param coords input coordinates array
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     * @param sb buffer to store the geojson
     */
    public static void toGeojsonCoordinates(Coordinate[] coords, int maxdecimaldigits, StringBuilder sb) {
        sb.append("[");
        for (int i = 0; i < coords.length; i++) {
            toGeojsonCoordinate(coords[i],maxdecimaldigits, sb);
            if (i < coords.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
    }

    /**
     * Convert a JTS coordinate to a GeoJSON representation.
     *
     * Only x, y and z values are supported.
     *
     * Syntax:
     *
     * [X,Y] or [X,Y,Z]
     *
     * @param coord input {@link Coordinate}
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     * @param sb buffer to store the geojson
     */
    public static void toGeojsonCoordinate(Coordinate coord, int maxdecimaldigits, StringBuilder sb) {
        sb.append("[");
        sb.append(CoordinateUtils.round(coord.x, maxdecimaldigits)).append(",").append(CoordinateUtils.round(coord.y, maxdecimaldigits));
        if (!Double.isNaN(coord.z)) {
            sb.append(",").append(CoordinateUtils.round(coord.z, maxdecimaldigits));
        }
        sb.append("]");
    }

    /**
     * Convert a JTS Envelope to a GeoJSON representation.
     *
     * @param e The envelope
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     *
     * @return The envelope encoded as GeoJSON
     */
    public String toGeoJsonEnvelope(Envelope e, int maxdecimaldigits) {
        return new StringBuffer().append("[").append(CoordinateUtils.round(e.getMinX(), maxdecimaldigits)).append(",")
                .append(CoordinateUtils.round(e.getMinY(), maxdecimaldigits)).append(",").append(CoordinateUtils.round(e.getMaxX(), maxdecimaldigits)).append(",")
                .append(CoordinateUtils.round(e.getMaxY(), maxdecimaldigits)).append("]").toString();
    }
}
