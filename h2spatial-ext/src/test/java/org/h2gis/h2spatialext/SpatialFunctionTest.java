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
package org.h2gis.h2spatialext;

import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Nicolas Fortin
 */
public class SpatialFunctionTest {
    private static Connection connection;
    private static final String DB_NAME = "SpatialFunctionTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME,false);
        CreateSpatialExtension.initSpatialExtension(connection);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void test_ST_ExplodeWithoutGeometryField() throws Exception  {
        Statement st = connection.createStatement();
        st.execute("CREATE TABLE forests ( fid INTEGER NOT NULL PRIMARY KEY, name CHARACTER VARYING(64)," +
                " boundary MULTIPOLYGON);");
        st.execute("INSERT INTO forests VALUES(109, 'Green Forest', ST_MPolyFromText( 'MULTIPOLYGON(((28 26,28 0,84 0," +
                "84 42,28 26), (52 18,66 23,73 9,48 6,52 18)),((59 18,67 18,67 13,59 13,59 18)))', 101))");
        ResultSet rs = st.executeQuery("SELECT ST_AsText(boundary) FROM ST_Explode('forests') WHERE name = 'Green Forest' and explod_id=2");
        assertTrue(rs.next());
        assertEquals("POLYGON ((59 18, 67 18, 67 13, 59 13, 59 18))", rs.getString(1));
        st.execute("drop table forests");
    }

    @Test
    public void test_ST_ExplodeEmptyGeometryCollection() throws Exception  {
        Statement st = connection.createStatement();
        st.execute("create table test(the_geom GEOMETRY, value Integer)");
        st.execute("insert into test VALUES (ST_GeomFromText('MULTILINESTRING EMPTY'),108)");
        st.execute("insert into test VALUES (ST_GeomFromText('MULTIPOINT EMPTY'),109)");
        st.execute("insert into test VALUES (ST_GeomFromText('MULTIPOLYGON EMPTY'),110)");
        st.execute("insert into test VALUES (ST_GeomFromText('GEOMETRYCOLLECTION EMPTY'),111)");
        ResultSet rs = st.executeQuery("SELECT the_geom , value FROM ST_Explode('test') ORDER BY value");
        assertTrue(rs.next());
        assertEquals(108,rs.getInt(2));
        assertEquals("LINESTRING EMPTY", rs.getString(1));
        assertTrue(rs.next());
        assertEquals(109,rs.getInt(2));
        assertEquals("POINT EMPTY", rs.getString(1));
        assertTrue(rs.next());
        assertEquals(110,rs.getInt(2));
        assertEquals("POLYGON EMPTY", rs.getString(1));
        assertTrue(rs.next());
        assertEquals(111,rs.getInt(2));
        assertEquals(null, rs.getObject(1));
        rs.close();
        st.execute("drop table test");
    }
}
