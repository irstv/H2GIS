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

package org.h2gis.functions.spatial.distance;

import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.linearref.LengthIndexedLine;

/**
 *
 * @author Erwan Bocher
 */
public class ST_ProjectPoint extends DeterministicScalarFunction{

    
    public ST_ProjectPoint(){
        addProperty(PROP_REMARKS, "Projet a point along a linestring. If the point projected is out of line "
                        + "the first or last point on the line will be returned otherwise"
                        + " the input point.");
    }
    @Override
    public String getJavaStaticMethod() {
        return "projectPoint";
    }
    
    
    /**
     * Project a point on a linestring or multilinestring
     * @param point {@link Point}
     * @param geometry {@link Geometry}
     * @return Geometry
     */
    public static Point projectPoint(Geometry point, Geometry geometry) throws SQLException {
        if (point == null || geometry==null) {
            return null;
        }
        if(point.isEmpty()||geometry.isEmpty()){
            return null;
        }
        if(point.getSRID()!=geometry.getSRID()){
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
        if (point.getDimension()==0 && geometry.getDimension() == 1) {
            LengthIndexedLine ll = new LengthIndexedLine(geometry);
            double index = ll.project(point.getCoordinate());
            return geometry.getFactory().createPoint(ll.extractPoint(index));
        } else {
            return null;
        }
    }
    
}
