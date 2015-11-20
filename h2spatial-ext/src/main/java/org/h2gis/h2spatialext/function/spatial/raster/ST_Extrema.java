/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.h2spatialext.function.spatial.raster;

import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.sql.SQLException;
import javax.media.jai.JAI;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;
import org.h2.api.GeoRaster;
import org.h2.util.RasterUtils;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.h2spatialext.jai.UnaryFunction;
import org.h2gis.h2spatialext.jai.UnaryFunctionDescriptor;

/**
 * Return the min and max value for one band of the input raster.
 *
 * @author Erwan Bocher
 */
public class ST_Extrema extends DeterministicScalarFunction {

    
    public ST_Extrema(){
        addProperty(PROP_REMARKS, "");
        UnaryFunctionDescriptor.register();
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "extrema";
    }

    /**
     * 
     * @param geoRaster
     * @return
     * @throws IOException
     * @throws SQLException 
     */
    public static double[] extrema(GeoRaster geoRaster) throws IOException, SQLException {
        if (geoRaster == null) {
            return null;
        }
        RasterUtils.RasterMetaData metaData = geoRaster.getMetaData();
        if (geoRaster.getMetaData().numBands != 1) {
            throw new IllegalArgumentException("ST_Extrema accept only raster with one band");
        }

        ParameterBlock pb = new ParameterBlock();
        pb.addSource(geoRaster);
        
        RasterUtils.RasterBandMetaData band = metaData.bands[0];
        
        if (band.hasNoData) {
            final double nodataValue = band.noDataValue;
            UnaryFunction<Double, Double> replaceNodata = new UnaryFunction<Double, Double>() {

                @Override
                public Double invoke(Double arg) {
                    if(Double.compare(arg, nodataValue) != 0) {
                        return 1d;
                    } else {
                        return 0d;
                    }
                }

            };
            ParameterBlock pbNodata = new ParameterBlock().addSource(geoRaster)
                    .add(replaceNodata);

            RenderedOp image = JAI.create("unaryfunction", pbNodata);

            ROI roi = new ROI(image, 0);
            pb.add(roi);
        }

        RenderedOp op = JAI.create("extrema", pb);

        // Retrieve both the maximum and minimum pixel value
        double[] allMins = (double[]) op.getProperty("minimum");
        double[] allMaxs = (double[]) op.getProperty("maximum");
        return new double[]{allMins[0], allMaxs[0]};
    }
    
}
