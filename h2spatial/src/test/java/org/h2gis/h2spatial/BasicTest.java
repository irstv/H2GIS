/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.h2spatial;

import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import static org.junit.Assert.*;
/**
 * 
 * @author Erwan Bocher
 */
public class BasicTest {
        private static Connection connection;

        @BeforeClass
        public static void tearUp() throws Exception {
            // Keep a connection alive to not close the DataBase on each unit test
            connection = SpatialH2UT.createSpatialDataBase("BasicTest");
        }
        @AfterClass
        public static void tearDown() throws Exception {
            connection.close();
        }
        @Test
        public void testPoints3D() throws Exception {

                WKTReader wktReader = new WKTReader();

                Geometry geom = wktReader.read("POINT(0 1 3)");

                Coordinate coord = geom.getCoordinates()[0];

                assertTrue(3 == coord.z);

        }

        @Test
        public void testWriteRead2DGeometry() throws ClassNotFoundException,
                SQLException, ParseException {
                final Statement stat = connection.createStatement();

                stat.execute("DROP TABLE IF EXISTS POINT2D");

                stat.execute("CREATE TABLE POINT2D (gid int , the_geom GEOMETRY)");
                stat.execute("INSERT INTO POINT2D (gid, the_geom) VALUES(1, ST_GeomFromText('POINT(0 12)', 27582))");


                ResultSet rs = stat.executeQuery("SELECT * from POINT2D;");
                ResultSetMetaData rsmd2 = rs.getMetaData();

                assertEquals(CreateSpatialExtension.GEOMETRY_BASE_TYPE,rsmd2.getColumnTypeName(2));

                /*
                ResultSetMetaData rsmd2 = rs.getMetaData();
                WKBReader wkbReader = new WKBReader();
                byte valObj[];
                Geometry geom;
                boolean hasGeometryColumn = false;

                for (; rs.next();) {
                        String columnTypeName = rsmd2.getColumnTypeName(2);

                        if (columnTypeName.equals(CreateSpatialExtension.GEOMETRY_BASE_TYPE)) {
                                valObj = rs.getBytes(2);
                                geom = wkbReader.read(valObj);
                                Coordinate coord = geom.getCoordinates()[0];
                                assertTrue(coord.x == 0);
                                assertTrue(coord.y == 12);
                                hasGeometryColumn = true;
                        }

                }
                assertTrue(hasGeometryColumn);
                */

                stat.close();
        }

        @Test
        public void testWriteRead3DGeometry() throws ClassNotFoundException,
                SQLException, ParseException {

                final Statement stat = connection.createStatement();

                stat.execute("DROP TABLE IF EXISTS POINT3D");

                stat.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY)");
                stat.execute("INSERT INTO POINT3D (gid, the_geom) VALUES(1, ST_GeomFromText('POINT(0 12 3)', 27582))");



                ResultSet rs = stat.executeQuery("SELECT * from POINT3D;");
                ResultSetMetaData rsmd2 = rs.getMetaData();

                assertEquals(CreateSpatialExtension.GEOMETRY_BASE_TYPE,rsmd2.getColumnTypeName(2));

                /*
                ResultSetMetaData rsmd2 = rs.getMetaData();
                WKBReader wkbReader = new WKBReader();
                byte valObj[];
                Geometry geom;
                boolean hasGeometryColumn = false;
                for (; rs.next();) {
                        String columnTypeName = rsmd2.getColumnTypeName(2);
                        if (columnTypeName.equalsIgnoreCase(CreateSpatialExtension.GEOMETRY_BASE_TYPE)) {
                                valObj = rs.getBytes(2);
                                geom = wkbReader.read(valObj);
                                Coordinate coord = geom.getCoordinates()[0];
                                assertTrue(coord.x == 0);
                                assertTrue(coord.y == 12);
                                assertTrue(coord.z == 3);
                                hasGeometryColumn = true;
                        }

                }
                assertTrue(hasGeometryColumn);
                */
                stat.close();

        }
        @Test
        public void testGeometryType() throws Exception {
            final Statement stat = connection.createStatement();
            stat.execute("DROP TABLE IF EXISTS GEOMTABLE;");
            stat.execute("CREATE TABLE GEOMTABLE (gid INTEGER AUTO_INCREMENT PRIMARY KEY, the_geom GEOMETRY);");
        }
        @Test
        public void testWriteRead3DGeometryWithNaNZ() throws ClassNotFoundException, SQLException, ParseException {

                final Statement stat = connection.createStatement();

                stat.execute("DROP TABLE IF EXISTS POINT3D");

                stat.execute("CREATE TABLE POINT3D (gid int , the_geom GEOMETRY)");
                stat.execute("INSERT INTO POINT3D (gid, the_geom) VALUES(1, ST_GeomFromText('POINT(0 12)', 27582))");

                ResultSet rs = stat.executeQuery("SELECT * from POINT3D;");
                ResultSetMetaData rsmd2 = rs.getMetaData();
                Geometry geom;
                boolean hasGeometryColumn = false;
                while(rs.next()) {
                        String columnTypeName = rsmd2.getColumnTypeName(2);
                        if (columnTypeName.equalsIgnoreCase(CreateSpatialExtension.GEOMETRY_BASE_TYPE)) {
                                geom = (Geometry)rs.getObject("the_geom");
                                Coordinate coord = geom.getCoordinates()[0];
                                assertTrue(coord.x == 0);
                                assertTrue(coord.y == 12);
                                assertTrue(Double.isNaN(coord.z));
                                hasGeometryColumn = true;
                        }

                }
                assertTrue(hasGeometryColumn);
                stat.close();
        }

        @Test
        public void testST_Area() throws Exception {
            final Statement stat = connection.createStatement();
            ResultSet rs = stat.executeQuery("select ST_Area(ST_GeomFromText('POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))', 27572)) as area from dummy");
            assertTrue(rs.next());
            assertEquals(100.0,rs.getDouble("area"),1e-12);
            stat.close();

        }
}
