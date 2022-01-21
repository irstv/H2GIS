/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.io.shp;

import org.h2.table.Column;
import org.h2.util.JdbcUtils;
import org.h2.value.TypeInfo;
import org.h2gis.api.DriverFunction;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.functions.io.DriverManager;
import org.h2gis.functions.io.dbf.DBFDriverFunction;
import org.h2gis.functions.io.dbf.internal.DbaseFileHeader;
import org.h2gis.functions.io.file_table.FileEngine;
import org.h2gis.functions.io.file_table.H2TableIndex;
import org.h2gis.functions.io.shp.internal.SHPDriver;
import org.h2gis.functions.io.shp.internal.ShapeType;
import org.h2gis.functions.io.shp.internal.ShapefileHeader;
import org.h2gis.functions.io.utility.PRJUtil;
import org.h2gis.utilities.GeometryTypeCodes;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.GeometryMetaData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.h2gis.utilities.FileUtilities;
import org.h2gis.utilities.GeometryTableUtilities;
import org.h2gis.utilities.Tuple;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.locationtech.jts.geom.Geometry;

/**
 * Read/Write Shape files
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class SHPDriverFunction implements DriverFunction {

    public static String DESCRIPTION = "ESRI shapefile";
    private static final int BATCH_MAX_SIZE = 200;

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName, null, progress);
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName, null, deleteFiles, progress);
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String options, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        progress = DriverManager.check(connection,tableReference, fileName, progress);
        if (!FileUtilities.isExtensionWellFormated(fileName, "shp")) {
            throw new SQLException("Only .shp extension is supported");
        }
        if (deleteFiles) {
            //Delete all shapeFile extensions
            String path = fileName.getAbsolutePath();
            String nameWithoutExt = path.substring(0, path.lastIndexOf('.'));
            Files.deleteIfExists(fileName.toPath());
            Files.deleteIfExists(new File(nameWithoutExt + ".dbf").toPath());
            Files.deleteIfExists(new File(nameWithoutExt + ".shx").toPath());
            Files.deleteIfExists(new File(nameWithoutExt + ".prj").toPath());
        }
        else{
            //If one of the files exist throw an error
            String path = fileName.getAbsolutePath();
            String nameWithoutExt = path.substring(0, path.lastIndexOf('.'));
            if(fileName.exists() || new File(nameWithoutExt + ".dbf").exists()
                    || new File(nameWithoutExt + ".shx").exists()||new File(nameWithoutExt + ".prj").exists()){
                throw new IOException("The file already exist.");
            }
        }
        final DBTypes dbType = DBUtils.getDBType(connection);
        String regex = ".*(?i)\\b(select|from)\\b.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tableReference);
        if (matcher.find()) {
            if (tableReference.startsWith("(") && tableReference.endsWith(")")) {
                PreparedStatement ps = connection.prepareStatement(tableReference, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                JDBCUtilities.attachCancelResultSet(ps, progress);
                ResultSet resultSet = ps.executeQuery();
                int recordCount = 0;
                resultSet.last();
                recordCount = resultSet.getRow();
                resultSet.beforeFirst();
                ProgressVisitor copyProgress = progress.subProcess(recordCount);
                Tuple<String, Integer> spatialFieldNameAndIndex = GeometryTableUtilities.getFirstGeometryColumnNameAndIndex(resultSet);
                String[] files = doExport(connection, spatialFieldNameAndIndex.second(), resultSet, recordCount, fileName, progress, options);
                copyProgress.endOfProgress();
                return files;
            } else {
                throw new SQLException("The select query must be enclosed in parenthesis: '(SELECT * FROM ORDERS)'.");
            }
        } else {
            TableLocation tableLocation = TableLocation.parse(tableReference, dbType);
            String location = tableLocation.toString(dbType);
            int recordCount = JDBCUtilities.getRowCount(connection, location);
            ProgressVisitor copyProgress = progress.subProcess(recordCount);
            // Read Geometry Index and type
            Tuple<String, Integer> spatialFieldNameAndIndex = GeometryTableUtilities.getFirstGeometryColumnNameAndIndex(connection, tableLocation);
            Statement st = connection.createStatement();
            JDBCUtilities.attachCancelResultSet(st, progress);
            ResultSet rs = st.executeQuery(String.format("select * from %s", location));
            String[] files = doExport(connection, spatialFieldNameAndIndex.second(), rs, recordCount, fileName, copyProgress, options);
            copyProgress.endOfProgress();
            return files;
        }
    }

    /**
     * Save a table or a query to a shpfile
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to write, if exists it may be replaced
     * @param encoding File encoding, null will use default encoding
     * @param progress to display the IO progress
     * @throws SQLException
     * @throws IOException
     */
    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String encoding, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName, encoding, false, progress);
    }

    /**
     * Method to export a resulset into a shapefile
     *
     * @param fileName File path to write, if exists it may be replaced
     * @param progress to display the IO progress
     * @param encoding File encoding, null will use default encoding
     * @throws java.sql.SQLException
     */
    private String[] doExport(Connection connection, Integer spatialFieldIndex, ResultSet rs, int recordCount, File fileName, ProgressVisitor progress, String encoding) throws SQLException, IOException {
        int srid = 0;
        ShapeType shapeType = null;
        try {
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            ArrayList<Integer> columnIndexes = new ArrayList<Integer>();
            DbaseFileHeader header = DBFDriverFunction.dBaseHeaderFromMetaData(resultSetMetaData, columnIndexes);
            columnIndexes.add(0, spatialFieldIndex);
            if (encoding != null && !encoding.isEmpty()) {
                header.setEncoding(encoding);
            }
            header.setNumRecords(recordCount);
            SHPDriver shpDriver = null;
            Object[] row = new Object[header.getNumFields() + 1];
            while (rs.next()) {
                int i = 0;
                for (Integer index : columnIndexes) {
                    row[i++] = rs.getObject(index);
                }
                if (shpDriver == null) {
                    // If there is not shape type constraint read the first geometry and use the same type
                    Geometry wkb = (Geometry) rs.getObject(spatialFieldIndex);
                    if (wkb != null) {
                        GeometryMetaData gm = GeometryMetaData.getMetaData(wkb);
                        if (srid == 0) {
                            srid = gm.SRID;
                        }
                        shapeType = getShapeTypeFromGeometryMetaData(gm);
                    }
                    if (shapeType != null) {
                        shpDriver = new SHPDriver();
                        shpDriver.setGeometryFieldIndex(0);
                        shpDriver.initDriver(fileName, shapeType, header);
                    } else {
                        throw new SQLException("Unsupported geometry type.");
                    }
                }
                if (shpDriver != null) {
                    shpDriver.insertRow(row);
                }
                progress.endStep();
            }
            if (shpDriver != null) {
                shpDriver.close();
            }
            if(srid>=0) {
                String path = fileName.getAbsolutePath();
                String nameWithoutExt = path.substring(0, path.lastIndexOf('.'));
                File outPrjFile = new File(nameWithoutExt + ".prj");
                PRJUtil.writePRJ(connection, srid,outPrjFile);
                return new String[]{shpDriver.shpFile.getAbsolutePath(), shpDriver.shxFile.getAbsolutePath(),
                        shpDriver.dbfFile.getAbsolutePath(), outPrjFile.getAbsolutePath()};
            }
            return new String[]{shpDriver.shpFile.getAbsolutePath(), shpDriver.shxFile.getAbsolutePath(), shpDriver.dbfFile.getAbsolutePath()};

        } finally {
            rs.close();
        }
    }

    @Override
    public String getFormatDescription(String format) {
        if (format.equalsIgnoreCase("shp")) {
            return DESCRIPTION;
        } else {
            return "";
        }
    }

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getImportFormats() {
        return new String[]{"shp"};
    }

    @Override
    public String[] getExportFormats() {
        return new String[]{"shp"};
    }

    @Override
    public boolean isSpatialFormat(String extension) {
        return extension.equalsIgnoreCase("shp");
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        return importFile(connection, tableReference, fileName, null, progress);
    }

    /**
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to read
     * @param forceEncoding If defined use this encoding instead of the one
     * defined in dbf header.
     * @param progress
     * @throws SQLException Table write error
     * @throws IOException File read error
     */
    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, String forceEncoding, ProgressVisitor progress) throws SQLException, IOException {
        return importFile(connection, tableReference, fileName, null, false, progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, boolean deleteTables, ProgressVisitor progress
    ) throws SQLException, IOException {
        return importFile(connection, tableReference, fileName, null, deleteTables, progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, String options, boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        progress = DriverManager.check(connection,tableReference,fileName, progress);
        final DBTypes dbType = DBUtils.getDBType(connection);
        if (FileUtilities.isFileImportable(fileName, "shp")) {
            TableLocation requestedTable = TableLocation.parse(tableReference, dbType);
            String outputTableName = requestedTable.toString();
            if (deleteTables) {
                Statement stmt = connection.createStatement();
                stmt.execute("DROP TABLE IF EXISTS " + outputTableName);
                stmt.close();
            }
            SHPDriver shpDriver = new SHPDriver();
            shpDriver.initDriverFromFile(fileName, options);
            ProgressVisitor copyProgress = progress.subProcess((int) (shpDriver.getRowCount() / BATCH_MAX_SIZE));
            String lastSql = "";
            int dbfNumFields =0;
            try {
                DbaseFileHeader dbfHeader = shpDriver.getDbaseFileHeader();
                ShapefileHeader shpHeader = shpDriver.getShapeFileHeader();
                final TableLocation parse;
                int srid;
                try (
                    // Build CREATE TABLE sql request
                    Statement st = connection.createStatement()) {
                    dbfNumFields = dbfHeader.getNumFields();
                    List<Column> otherCols = new ArrayList<Column>(dbfNumFields + 1);
                    otherCols.add(new Column("THE_GEOM", TypeInfo.TYPE_GEOMETRY));
                    String types = DBFDriverFunction.getSQLColumnTypes(dbfHeader, DBUtils.getDBType(connection), otherCols);
                    if (!types.isEmpty()) {
                        types = ", " + types;
                    }
                    String pkColName = FileEngine.getUniqueColumnName(H2TableIndex.PK_COLUMN_NAME, otherCols);
                    srid = PRJUtil.getSRID(shpDriver.prjFile);
                    shpDriver.setSRID(srid);
                    st.execute(String.format("CREATE TABLE %s (" + pkColName + " INT PRIMARY KEY , the_geom GEOMETRY(%s, %d) %s)", requestedTable,
                                getSFSGeometryType(shpHeader), srid, types));

                }
                try {
                    connection.setAutoCommit(false);
                    lastSql = String.format("INSERT INTO %s VALUES (?, %s )", outputTableName,
                            DBFDriverFunction.getQuestionMark(dbfNumFields + 1));
                    final int columnCount = dbfNumFields+1;
                    connection.setAutoCommit(false);
                    try (PreparedStatement preparedStatement = connection.prepareStatement(lastSql)) {
                        long batchSize = 0;
                        for (int rowId = 0; rowId < shpDriver.getRowCount(); rowId++) {
                            //Set the PK
                            preparedStatement.setInt(1, rowId+1);
                            for (int columnId = 0; columnId < columnCount; columnId++) {
                                JdbcUtils.set(preparedStatement,columnId + 2, shpDriver.getField(rowId, columnId), null);
                            }
                            preparedStatement.addBatch();
                            batchSize++;
                            if (batchSize >= BATCH_MAX_SIZE) {
                                preparedStatement.executeBatch();
                                connection.commit();
                                preparedStatement.clearBatch();
                                batchSize = 0;
                                copyProgress.endStep();
                            }
                        }
                        if (batchSize > 0) {
                            preparedStatement.executeBatch();
                            connection.commit();
                        }
                        return new String[]{outputTableName};
                    }
                } catch (Exception ex) {
                    connection.createStatement().execute("DROP TABLE IF EXISTS " + outputTableName);
                    connection.commit();
                    throw new SQLException(ex.getLocalizedMessage(), ex);
                }
            } catch (SQLException ex) {
                throw new SQLException(lastSql + "\n" + ex.getLocalizedMessage(), ex);
            } finally {
                connection.setAutoCommit(true);
                shpDriver.close();
                copyProgress.endOfProgress();
                connection.setAutoCommit(true);
            }
        }
        return null;
    }

    /**
     * Return the shape type supported by the shapefile format
     *
     * @param meta
     * @return
     * @throws SQLException
     */
    private static ShapeType getShapeTypeFromGeometryMetaData(GeometryMetaData meta) throws SQLException {
        ShapeType shapeType;
        switch (meta.geometryTypeCode) {
            case GeometryTypeCodes.MULTILINESTRING:
            case GeometryTypeCodes.LINESTRING:
            case GeometryTypeCodes.MULTILINESTRINGM:
            case GeometryTypeCodes.LINESTRINGM:
            case GeometryTypeCodes.MULTILINESTRINGZ:
            case GeometryTypeCodes.LINESTRINGZ:
                shapeType = meta.hasZ ? ShapeType.ARCZ : ShapeType.ARC;
                break;
            case GeometryTypeCodes.POINTM:
            case GeometryTypeCodes.POINTZ:
            case GeometryTypeCodes.POINT:
                shapeType = meta.hasZ ? ShapeType.POINTZ : ShapeType.POINT;
                break;
            case GeometryTypeCodes.MULTIPOINTZ:
            case GeometryTypeCodes.MULTIPOINTM:
            case GeometryTypeCodes.MULTIPOINT:
                shapeType = meta.hasZ ? ShapeType.MULTIPOINTZ : ShapeType.MULTIPOINT;
                break;
            case GeometryTypeCodes.POLYGONZ:
            case GeometryTypeCodes.MULTIPOLYGONZ:
            case GeometryTypeCodes.POLYGONM:
            case GeometryTypeCodes.MULTIPOLYGONM:
            case GeometryTypeCodes.POLYGON:
            case GeometryTypeCodes.MULTIPOLYGON:
                shapeType = meta.hasZ ? ShapeType.POLYGONZ : ShapeType.POLYGON;
                break;
            default:
                return null;
        }
        return shapeType;
    }

    private static String getSFSGeometryType(ShapefileHeader header) {
        switch (header.getShapeType().id) {
            case 1:
                return "POINT";
            case 11:
            case 21:
                return "POINTZ";
            case 3:
                return "MULTILINESTRING";
            case 13:
            case 23:
                return "MULTILINESTRINGZ";
            case 5:
                return "MULTIPOLYGON";
            case 15:
            case 25:
                return "MULTIPOLYGONZ";
            case 8:
                return "MULTIPOINT";
            case 18:
            case 28:
                return "MULTIPOINTZ";
            default:
                return "GEOMETRY";
        }
    }
}
