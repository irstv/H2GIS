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

package org.h2gis.functions.spatial.create;

import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.locationtech.jts.geom.*;

import java.sql.*;
import org.h2gis.utilities.GeometryMetaData;
import org.h2gis.utilities.GeometryTableUtilities;
import org.h2gis.utilities.Tuple;

/**
 * GridRowSet is used to populate a result set with all grid cells. A cell could
 * be represented as a polygon or its center point.
 *
 * @author Erwan Bocher
 */
public class GridRowSet implements SimpleRowSource {

    private static final GeometryFactory GF = new GeometryFactory();
    private static int cellI = 0;
    private static int cellJ = 0;
    private int maxI, maxJ;
    private double deltaX, deltaY;
    private double minX, minY;
    private int id = 0;
    private final Connection connection;
    private boolean firstRow = true;
    private Envelope envelope;
    private boolean isTable;
    private String tableName;
    private boolean isCenterCell = false;
    private int srid;

    /**
     * The grid will be computed according a table stored in the database
     *
     * @param connection
     * @param deltaX
     * @param deltaY
     * @param tableName
     */
    public GridRowSet(Connection connection, double deltaX, double deltaY, String tableName) {
        this.connection = connection;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.tableName = tableName;
        this.isTable = true;
    }

    /**
     * The grid will be computed according the envelope of a geometry
     *
     * @param connection
     * @param deltaX
     * @param deltaY
     * @param geometry 
     */
    public GridRowSet(Connection connection, double deltaX, double deltaY, Geometry geometry) {
        this.connection = connection;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        srid = geometry.getSRID();        
        this.envelope = geometry.getEnvelopeInternal();
        this.isTable = false;
    }

    @Override
    public Object[] readRow() throws SQLException {
        if (firstRow) {
            reset();
        }
        if (cellI == maxI) {
            cellJ++;
            cellI = 0;
        }
        if (cellJ >= maxJ) {
            cellJ = 0;
            return null;
        }
        if (isCenterCell) {
            return new Object[]{getCellPoint(), id++, cellI, cellJ + 1};
        }
        return new Object[]{getCellPolygon(), id++, cellI, cellJ + 1};
    }

    @Override
    public void close() {
    }

    @Override
    public void reset() throws SQLException {
        cellI = 0;
        cellJ = 0;
        firstRow = false;
        //We compute the extend according the first input value
        if (isTable) {
            Statement statement = connection.createStatement();
            //Find the SRID
            Tuple<String, GeometryMetaData> geomMetadata = GeometryTableUtilities.getFirstColumnMetaData(connection, TableLocation.parse(tableName, JDBCUtilities.isH2DataBase(connection)));
            srid = geomMetadata.second().SRID;
             try (ResultSet rs = statement.executeQuery("select ST_Extent(" + geomMetadata.first() + ")  from " + tableName)) {
                rs.next();
                Geometry geomExtend = (Geometry) rs.getObject(1);
                if (geomExtend == null) {
                    throw new SQLException("The envelope cannot be null.");
                } else {
                    envelope = geomExtend.getEnvelopeInternal();
                    initParameters();
                }

            }
        } else {
            if (envelope == null) {
                throw new SQLException("The input geometry used to compute the grid cannot be null.");
            } else {
                initParameters();
            }
        }
    }

    /**
     * Compute the polygon corresponding to the cell
     *
     * @return Polygon of the cell
     */
    private Polygon getCellPolygon() {
        final Coordinate[] summits = new Coordinate[5];
        double x1 = minX + cellI * deltaX;
        double y1 = minY + cellJ * deltaY;
        double x2 = minX + (cellI + 1) * deltaX;
        double y2 = minY + (cellJ + 1) * deltaY;
        summits[0] = new Coordinate(x1, y1);
        summits[1] = new Coordinate(x2, y1);
        summits[2] = new Coordinate(x2, y2);
        summits[3] = new Coordinate(x1, y2);
        summits[4] = new Coordinate(x1, y1);
        final LinearRing g = GF.createLinearRing(summits);
        final Polygon gg = GF.createPolygon(g, null);
        cellI++;
        gg.setSRID(srid);
        return gg;
    }

    /**
     * Compute the point of the cell
     *
     * @return Center point of the cell
     */
    private Point getCellPoint() {
        double x1 = (minX + cellI * deltaX) + (deltaX / 2d);
        double y1 = (minY + cellJ * deltaY) + (deltaY / 2d);
        cellI++;
        Point gg = GF.createPoint(new Coordinate(x1, y1));
        gg.setSRID(srid);
        return gg;
    }

    /**
     * Return true is cell is represented as point, false as a polygon
     *
     * @return
     */
    public boolean isCenterCell() {
        return isCenterCell;
    }

    /**
     * Set if the cell must be represented as a point or a polygon
     *
     * @param isCenterCell
     */
    public void setCenterCell(boolean isCenterCell) {
        this.isCenterCell = isCenterCell;
    }
   

    /**
     * Compute the parameters need to create each cells
     *
     */
    private void initParameters() {
        this.minX = envelope.getMinX();
        this.minY = envelope.getMinY();
        double cellWidth = envelope.getWidth();
        double cellHeight = envelope.getHeight();
        this.maxI = (int) Math.ceil(cellWidth
                / deltaX);
        this.maxJ = (int) Math.ceil(cellHeight
                / deltaY);
    }

    /**
     * Give the regular grid
     *
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet getResultSet() throws SQLException {
        SimpleResultSet srs = new SimpleResultSet(this);
        srs.addColumn("THE_GEOM", Types.OTHER, "GEOMETRY", 0, 0);
        srs.addColumn("ID", Types.INTEGER, 10, 0);
        srs.addColumn("ID_COL", Types.INTEGER, 10, 0);
        srs.addColumn("ID_ROW", Types.INTEGER, 10, 0);
        return srs;
    }
}
