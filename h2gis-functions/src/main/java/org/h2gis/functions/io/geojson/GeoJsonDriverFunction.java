/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.geojson;

import org.h2gis.api.DriverFunction;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * GeoJSON driver to import a GeoJSON file and export a spatial table in a
 * GeoJSON 1.0 file.
 * 
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class GeoJsonDriverFunction implements DriverFunction {

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getImportFormats() {
        return new String[]{"geojson"};
    }

    @Override
    public String[] getExportFormats() {
        return new String[]{"geojson"};
    }

    @Override
    public String getFormatDescription(String format) {
        if (format.equalsIgnoreCase("geojson")) {
            return "GeoJSON 1.0";
        } else {
            return "";
        }
    }

    @Override
    public boolean isSpatialFormat(String extension) {
        return extension.equals("geojson");
    }

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException{
        exportTable(connection,tableReference, fileName,progress, null);
    }
    
    /**
     * Export a table or a query to a geojson file
     * 
     * @param connection
     * @param tableReference
     * @param fileName
     * @param progress
     * @param encoding
     * @throws SQLException
     * @throws IOException 
     */
    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress, String encoding) throws SQLException, IOException{
        GeoJsonWriteDriver geoJsonDriver = new GeoJsonWriteDriver(connection);
        geoJsonDriver.write(progress,tableReference, fileName, encoding);
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
            throws SQLException, IOException {
        GeoJsonReaderDriver geoJsonReaderDriver = new GeoJsonReaderDriver(connection, fileName);
        geoJsonReaderDriver.read(progress, tableReference);
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress,
                           String options) throws SQLException, IOException {
        importFile(connection, tableReference, fileName, progress);
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress,
                           boolean deleteTables) throws SQLException, IOException {

        if(deleteTables) {
            final boolean isH2 = JDBCUtilities.isH2DataBase(connection);
            TableLocation requestedTable = TableLocation.parse(tableReference, isH2);
            String table = requestedTable.getTable();
            Statement stmt = connection.createStatement();
            stmt.execute("DROP TABLE IF EXISTS " + table);
            stmt.close();
        }

        importFile(connection, tableReference, fileName, progress);
    }
}
