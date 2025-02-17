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

package org.h2gis.functions.spatial.crs;

import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;
import org.locationtech.jts.geom.Geometry;


/**
 * Return a new geometry with a replaced spatial reference id.
 * @author Nicolas Fortin
 */
public class ST_SetSRID  extends AbstractFunction implements ScalarFunction {
    public ST_SetSRID() {
        addProperty(PROP_REMARKS, "Return a new geometry with a replaced spatial reference id. Warning, use ST_Transform" +
                " if you want to change the coordinate reference system as this method does not update the coordinates." +
                " This function can take at first argument an instance of Geometry or Envelope");
    }

    @Override
    public String getJavaStaticMethod() {
        return "setSRID";
    }

    /**
     * Set a new SRID to the geometry
     * @param geometry {@link Geometry}
     * @param srid srid code
     * @return Geometry with an srid
     */
    public static Geometry setSRID(Geometry geometry, Integer srid) throws IllegalArgumentException {
        if (geometry == null) {
            return null;
        }
        if(geometry.isEmpty()){
            return geometry;
        }
        if (srid == null) {
            throw new IllegalArgumentException("The SRID code cannot be null.");
        }
        Geometry geom = geometry.copy();
        geom.setSRID(srid);
        return geom;
    }
}
