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
package org.h2gis.functions.spatial.metadata;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.h2.jdbc.JdbcSQLException;
import org.h2gis.utilities.GeometryMetaData;
import org.h2gis.utilities.GeometryTableUtils;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;

import static org.junit.jupiter.api.Assertions.*;
import org.locationtech.jts.geom.Envelope;

public class GeometryTableUtilsTest {

    private static Connection connection;
    private Statement st;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(GeometryTableUtilsTest.class.getSimpleName());
        connection.createStatement().execute("DROP TABLE IF EXISTS NOGEOM");
        connection.createStatement().execute("CREATE TABLE NOGEOM (id INT, str VARCHAR(100))");
        connection.createStatement().execute("INSERT INTO NOGEOM VALUES (25, 'twenty five')");
        connection.createStatement().execute("INSERT INTO NOGEOM VALUES (6, 'six')");

        connection.createStatement().execute("DROP TABLE IF EXISTS POINTTABLE");
        connection.createStatement().execute("CREATE TABLE POINTTABLE (geom GEOMETRY)");
        connection.createStatement().execute("INSERT INTO POINTTABLE VALUES ('POINT(1 1)')");

        connection.createStatement().execute("DROP TABLE IF EXISTS GEOMTABLE");
        connection.createStatement().execute("CREATE TABLE GEOMTABLE (geom GEOMETRY, pt GEOMETRY(  POINTZM    ), linestr LINESTRING, "
                + "plgn POLYGON, multipt MULTIPOINT, multilinestr MULTILINESTRING, multiplgn MULTIPOLYGON, "
                + "geomcollection GEOMCOLLECTION)");
        connection.createStatement().execute("INSERT INTO GEOMTABLE VALUES ('POINT(1 1)', 'POINT(1 1 0 0)',"
                + " 'LINESTRING(1 1, 2 2)', 'POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))', 'MULTIPOINT((1 1))',"
                + " 'MULTILINESTRING((1 1, 2 2))', 'MULTIPOLYGON(((1 1, 1 2, 2 2, 2 1, 1 1)))',"
                + " 'GEOMETRYCOLLECTION(POINT(1 1))')");
        connection.createStatement().execute("INSERT INTO GEOMTABLE VALUES ('LINESTRING(1 1, 2 2)', 'POINT(2 2 0 0)',"
                + " 'LINESTRING(2 2, 1 1)', 'POLYGON((1 1, 1 3, 3 3, 3 1, 1 1))', 'MULTIPOINT((3 3))',"
                + " 'MULTILINESTRING((1 1, 3 3))', 'MULTIPOLYGON(((1 1, 1 3, 3 3, 3 1, 1 1)))',"
                + " 'GEOMETRYCOLLECTION(POINT(3 3))')");
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @BeforeEach
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @AfterEach
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @Test
    public void testGeometryMetadataFunctions() throws Exception {
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY)");
        st.execute("DROP VIEW IF EXISTS geo_cols; CREATE VIEW geo_cols AS "
                + "SELECT  f_table_catalog, "
                + " f_table_schema, "
                + " f_table_name, "
                + " f_geometry_column, "
                + "1 storage_type, "
                + "t[1] as geometry_type, "
                + "t[2] as coord_dimension, "
                + "t[3] as srid, "
                + "t[4] as type "
                + "FROM (SELECT TABLE_CATALOG f_table_catalog, "
                + "TABLE_SCHEMA f_table_schema, "
                + "TABLE_NAME f_table_name, "
                + "COLUMN_NAME f_geometry_column, "
                + "1 storage_type, FindGeometryMetadata(TABLE_CATALOG,TABLE_SCHEMA,TABLE_NAME, "
                + " COLUMN_NAME, COLUMN_TYPE) as t FROM INFORMATION_SCHEMA.COLUMNS"
                + " WHERE TYPE_NAME = 'GEOMETRY'); ");
        ResultSet rs = st.executeQuery("SELECT * FROM geo_cols");
        rs.next();
        assertEquals("DBH2FINDGEOMETRYMETADATATEST", rs.getString("f_table_catalog"));
        assertEquals("PUBLIC", rs.getString("f_table_schema"));
        assertEquals("GEO_POINT", rs.getString("f_table_name"));
        assertEquals("THE_GEOM", rs.getString("f_geometry_column"));
        assertEquals(1, rs.getInt("storage_type"));
        assertEquals(0, rs.getInt("geometry_type"));
        assertEquals(2, rs.getInt("coord_dimension"));
        assertEquals(0, rs.getInt("srid"));
        assertEquals("GEOMETRY", rs.getString("type"));

        //Alter the geometry
        st.execute("ALTER TABLE GEO_POINT ALTER COLUMN THE_GEOM type geometry(POINT Z, 4326)");
        rs = st.executeQuery("SELECT * FROM geo_cols");
        rs.next();
        assertEquals("DBH2FINDGEOMETRYMETADATATEST", rs.getString("f_table_catalog"));
        assertEquals("PUBLIC", rs.getString("f_table_schema"));
        assertEquals("GEO_POINT", rs.getString("f_table_name"));
        assertEquals("THE_GEOM", rs.getString("f_geometry_column"));
        assertEquals(1, rs.getInt("storage_type"));
        assertEquals(1001, rs.getInt("geometry_type"));
        assertEquals(3, rs.getInt("coord_dimension"));
        assertEquals(4326, rs.getInt("srid"));
        assertEquals("POINTZ", rs.getString("type"));

        st.execute("drop table if exists geo_linestring; CREATE TABLE geo_linestring (the_geom GEOMETRY(LINESTRINGZM, 4326))");
        rs = st.executeQuery("SELECT * FROM geo_cols where f_table_name= 'GEO_LINESTRING' ");
        rs.next();
        assertEquals("DBH2FINDGEOMETRYMETADATATEST", rs.getString("f_table_catalog"));
        assertEquals("PUBLIC", rs.getString("f_table_schema"));
        assertEquals("GEO_LINESTRING", rs.getString("f_table_name"));
        assertEquals("THE_GEOM", rs.getString("f_geometry_column"));
        assertEquals(1, rs.getInt("storage_type"));
        assertEquals(3002, rs.getInt("geometry_type"));
        assertEquals(4, rs.getInt("coord_dimension"));
        assertEquals(4326, rs.getInt("srid"));
        assertEquals("LINESTRINGZM", rs.getString("type"));
    }

    @Test
    public void testGeometryMetadataUtils() throws Exception {
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY)");
        GeometryMetaData geomMetadata = GeometryTableUtils.getMetaData(connection, TableLocation.parse("GEO_POINT"), "THE_GEOM");
        assertEquals("GEOMETRY", geomMetadata.geometryType);
        assertEquals("GEOMETRY", geomMetadata.sfs_geometryType);
        assertEquals(2, geomMetadata.dimension);
        assertEquals(0, geomMetadata.SRID);
        assertFalse(geomMetadata.hasZ);
        assertFalse(geomMetadata.hasM);
        st.execute("ALTER TABLE GEO_POINT ALTER COLUMN THE_GEOM type geometry(POINT Z, 4326)");
        geomMetadata = GeometryTableUtils.getMetaData(connection, TableLocation.parse("GEO_POINT"), "THE_GEOM");
        assertEquals("POINTZ", geomMetadata.geometryType);
        assertEquals("POINT", geomMetadata.sfs_geometryType);
        assertEquals(3, geomMetadata.dimension);
        assertEquals(4326, geomMetadata.SRID);
        assertTrue(geomMetadata.hasZ);
        assertFalse(geomMetadata.hasM);
        st.execute("ALTER TABLE GEO_POINT ALTER COLUMN THE_GEOM type geometry(POINTZM)");
        geomMetadata = GeometryTableUtils.getMetaData(connection, TableLocation.parse("GEO_POINT"), "THE_GEOM");
        assertEquals("POINTZM", geomMetadata.geometryType);
        assertEquals("POINT", geomMetadata.sfs_geometryType);
        assertEquals(4, geomMetadata.dimension);
        assertEquals(0, geomMetadata.SRID);
        assertTrue(geomMetadata.hasZ);
        assertTrue(geomMetadata.hasM);
    }
    
     @Test
    public void testGeometryMetadataUtils2() throws Exception {
        st.execute("drop table if exists geo_point; CREATE TABLE geo_point (the_geom GEOMETRY, geom GEOMETRY(POINT Z,4326))");
        Map<String, GeometryMetaData> geomMetadatas = GeometryTableUtils.getMetaData(connection, TableLocation.parse("GEO_POINT"));
        GeometryMetaData geomMetadata = geomMetadatas.get(0);
        assertEquals("GEOMETRY", geomMetadata.geometryType);
        assertEquals("GEOMETRY", geomMetadata.sfs_geometryType);
        assertEquals(2, geomMetadata.dimension);
        assertEquals(0, geomMetadata.SRID);
        assertFalse(geomMetadata.hasZ);
        assertFalse(geomMetadata.hasM);
        geomMetadata = geomMetadatas.get(1);        
        assertEquals("POINTZ", geomMetadata.geometryType);
        assertEquals("POINT", geomMetadata.sfs_geometryType);
        assertEquals(3, geomMetadata.dimension);
        assertEquals(4326, geomMetadata.SRID);
        assertTrue(geomMetadata.hasZ);
        assertFalse(geomMetadata.hasM);
        st.execute("ALTER TABLE GEO_POINT ALTER COLUMN THE_GEOM type geometry(POINTZM)");
        geomMetadatas = GeometryTableUtils.getMetaData(connection, TableLocation.parse("GEO_POINT"));
        geomMetadata = geomMetadatas.get(0);
        assertEquals("POINTZM", geomMetadata.geometryType);
        assertEquals("POINT", geomMetadata.sfs_geometryType);
        assertEquals(4, geomMetadata.dimension);
        assertEquals(0, geomMetadata.SRID);
        assertTrue(geomMetadata.hasZ);
        assertTrue(geomMetadata.hasM);
    }

    @Test
    public void testFirstGeometryFieldName1() throws Exception {
        st.execute("DROP TABLE IF EXISTS POINT3D");
        st.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY)");
        st.execute("INSERT INTO POINT3D (gid, the_geom) VALUES(1, ST_GeomFromText('POINT(0 12)', 27582))");
        ResultSet rs = st.executeQuery("SELECT * from POINT3D;");
        String geomField = GeometryTableUtils.getFirstGeometryFieldName(rs);
        assertEquals("THE_GEOM", geomField);
    }

    @Test
    public void testFirstGeometryFieldName2() throws Throwable {
        assertThrows(SQLException.class, () -> {
            try {
                st.execute("DROP TABLE IF EXISTS POINT3D");
                st.execute("CREATE TABLE POINT3D (gid int )");
                st.execute("INSERT INTO POINT3D (gid) VALUES(1)");
                ResultSet rs = st.executeQuery("SELECT * from POINT3D;");
                GeometryTableUtils.getFirstGeometryFieldName(rs);
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void testHasGeometryField() throws SQLException {
        st.execute("DROP TABLE IF EXISTS POINT3D");
        st.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY)");
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM POINT3D");
        assertTrue(GeometryTableUtils.hasGeometryField(rs));
        st.execute("DROP TABLE IF EXISTS POINT3D");
        st.execute("CREATE TABLE POINT3D (gid int)");
        rs = connection.createStatement().executeQuery("SELECT * FROM POINT3D");
        assertFalse(GeometryTableUtils.hasGeometryField(rs));
    }

    // getResultSetEnvelope(ResultSet resultSet)
    @Test
    public void testResultSetEnvelope1() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM GEOMTABLE");
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0), GeometryTableUtils.getEnvelope(rs));
    }

    @Test
    public void testResultSetEnvelope2() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM NOGEOM");
        assertThrows(SQLException.class, ()
                -> GeometryTableUtils.getEnvelope(rs));
    }

    @Test
    public void testResultSetEnvelope3() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM GEOMTABLE");
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0), GeometryTableUtils.getEnvelope(rs, "GEOM"));
        rs = connection.createStatement().executeQuery("SELECT * FROM GEOMTABLE");
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0), GeometryTableUtils.getEnvelope(rs, "MULTILINESTR"));
    }

    @Test
    public void testResultSetEnvelope4() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM NOGEOM");
        assertThrows(SQLException.class, ()
                -> GeometryTableUtils.getEnvelope(rs, "GEOM"));
    }
    
    @Test
    public void testGeometryFields1() throws SQLException {
        List<String> list = GeometryTableUtils.getGeometryFields(connection, TableLocation.parse("GEOMTABLE"));
        assertEquals(8, list.size());
        assertTrue(list.contains("GEOM"));
        assertTrue(list.contains("PT"));
        assertTrue(list.contains("LINESTR"));
        assertTrue(list.contains("PLGN"));
        assertTrue(list.contains("MULTIPT"));
        assertTrue(list.contains("MULTILINESTR"));
        assertTrue(list.contains("MULTIPLGN"));
        assertTrue(list.contains("GEOMCOLLECTION"));
    }
    
     @Test
    public void testGetSRID() throws SQLException {
        assertEquals(0, GeometryTableUtils.getSRID(connection, TableLocation.parse("GEOMTABLE")));
        assertEquals(0, GeometryTableUtils.getSRID(connection, TableLocation.parse("NOGEOM")));
        st.execute("DROP TABLE IF EXISTS POINT3D");
        st.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY(POINTZ, 4326))");
        assertEquals(4326, GeometryTableUtils.getSRID(connection, TableLocation.parse("POINT3D")));
       
    }
    
    /**
     * Check constraint pass
     * @throws SQLException
     */
    @Test
    public void testColumnSRIDGeometryColumns3() throws SQLException {
        st.execute("drop table IF EXISTS T_SRID");
        st.execute("create table T_SRID (the_geom GEOMETRY (GEOMETRY, 4326))");
        st.execute ("insert into T_SRID VALUES(ST_GEOMFROMTEXT('POINT (2 47)',4326))");
        try (ResultSet rs = st.executeQuery("SELECT SRID FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'T_SRID'")) {
            assertTrue(rs.next());
            assertEquals(4326, rs.getInt("srid"));
            assertFalse(rs.next());
        }
        assertEquals(4326, GeometryTableUtils.getSRID(connection, TableLocation.parse("T_SRID")));
    }
    
    /**
     * Check constraint pass
     * @throws SQLException
     */
    @Test
    public void testColumnSRIDGeometryColumns() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table IF EXISTS T_SRID");
        st.execute("create table T_SRID (the_geom GEOMETRY(GEOMETRY, 27572))");
        try (ResultSet rs = st.executeQuery("SELECT SRID FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'T_SRID'")) {
            assertTrue(rs.next());
            assertEquals(27572, rs.getInt("srid"));
            assertFalse(rs.next());
        }
    }

    /**
     * Check constraint pass
     * @throws SQLException
     */
    @Test
    public void testColumnSRIDGeometryColumns2() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table IF EXISTS T_SRID");
        st.execute("create table T_SRID (the_geom GEOMETRY (GEOMETRY, 27572))");
        try (ResultSet rs = st.executeQuery("SELECT SRID FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'T_SRID'")) {
            assertTrue(rs.next());
            assertEquals(27572, rs.getInt("srid"));
            assertFalse(rs.next());
        }
    }   
    
    
    /**
     * Check constraint pass
     * @throws SQLException
     */
    @Test
    public void testColumnSRIDGeometryColumns4() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table IF EXISTS T_SRID");
        st.execute("create table T_SRID (the_geom GEOMETRY (POINT, 4326))");
        st.execute ("insert into T_SRID VALUES(ST_GEOMFROMTEXT('POINT (2 47)',4326))");
        try (ResultSet rs = st.executeQuery("SELECT SRID FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'T_SRID'")) {
            assertTrue(rs.next());
            assertEquals(4326, rs.getInt("srid"));
            assertFalse(rs.next());
        }
        assertEquals(4326, GeometryTableUtils.getSRID(connection, TableLocation.parse("T_SRID")));
    }
    
    /**
     * Check constraint pass
     * @throws SQLException
     */
    @Test
    public void testColumnSRIDGeometryColumns5() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop table IF EXISTS T_SRID");
        st.execute("create table T_SRID (the_geom GEOMETRY (MULTIPOLYGON, 4326))");
        st.execute ("insert into T_SRID VALUES(ST_GEOMFROMTEXT('MULTIPOLYGON(((28 26,28 0,84 0,"
                + "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 4326))");
        try (ResultSet rs = st.executeQuery("SELECT SRID FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'T_SRID'")) {
            assertTrue(rs.next());
            assertEquals(4326, rs.getInt("srid"));
            assertFalse(rs.next());
        }
        assertEquals(4326, GeometryTableUtils.getSRID(connection, TableLocation.parse("T_SRID")));
    }
    
    
    @Test
    public void testSFSUtilities() throws Exception {
        String catalog = connection.getCatalog();
        st.execute("drop schema if exists blah");
        st.execute("create schema blah");
        st.execute("create table blah.testSFSUtilities(id integer, the_geom GEOMETRY(point))");
        List<String> geomFields = GeometryTableUtils.getGeometryFields(connection, new TableLocation(catalog, "blah", "testSFSUtilities"));
        assertEquals(1, geomFields.size());
        assertEquals("THE_GEOM", geomFields.get(0));
    }
    
     @Test
    public void testTableEnvelope() throws SQLException {
        TableLocation tableLocation = TableLocation.parse("GEOMTABLE");
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0),
                GeometryTableUtils.getEnvelope(connection, tableLocation, ""));
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0),
                GeometryTableUtils.getEnvelope(connection, tableLocation, "GEOM"));
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0),
                GeometryTableUtils.getEnvelope(connection, tableLocation, "PT"));
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0),
                GeometryTableUtils.getEnvelope(connection, tableLocation, "LINESTR"));
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                GeometryTableUtils.getEnvelope(connection, tableLocation, "PLGN"));
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                GeometryTableUtils.getEnvelope(connection, tableLocation, "MULTIPT"));
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                GeometryTableUtils.getEnvelope(connection, tableLocation, "MULTILINESTR"));
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                GeometryTableUtils.getEnvelope(connection, tableLocation, "MULTIPLGN"));
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                GeometryTableUtils.getEnvelope(connection, tableLocation, "GEOMCOLLECTION"));
    }

    @Test
    public void testBadTableEnvelope() throws SQLException {
        TableLocation tableLocation = TableLocation.parse("NOGEOM");
        assertThrows(SQLException.class,() ->
                GeometryTableUtils.getEnvelope(connection, tableLocation, ""));
    }
     @Test
    public void testEstimatedExtentWithoutIndex() throws SQLException {
        TableLocation tableLocation = TableLocation.parse("GEOMTABLE");
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0),
                GeometryTableUtils.getEstimatedExtent(connection, tableLocation, "GEOM").getEnvelopeInternal());
    }
    @Test
    public void testEstimatedExtentWithIndex() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS GEOMTABLE_INDEX; CREATE TABLE GEOMTABLE_INDEX (THE_GEOM GEOMETRY);");
        st.execute("INSERT INTO GEOMTABLE_INDEX VALUES ('POLYGON ((150 360, 200 360, 200 310, 150 310, 150 360))'),('POLYGON ((195.5 279, 240 279, 240 250, 195.5 250, 195.5 279))' )");
        st.execute("CREATE SPATIAL INDEX ON GEOMTABLE_INDEX(THE_GEOM)");
        TableLocation tableLocation = TableLocation.parse("GEOMTABLE_INDEX");
        assertEquals(new Envelope(150.0 , 240.0, 250.0 , 360.0),
                GeometryTableUtils.getEstimatedExtent(connection, tableLocation, "THE_GEOM").getEnvelopeInternal());
    }
    

}
