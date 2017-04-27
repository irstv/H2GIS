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

package org.h2gis.ext;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Registers the SQL functions contained in h2gis-functions and h2-network.
 *
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class H2GISExtension {  
     
    /**
     * Init H2 DataBase with all H2GIS functions
     *
     * @param connection Active connection
     * @throws SQLException
     */
    public static void load(Connection connection) throws SQLException {
        org.h2gis.functions.factory.H2GISFunctions.load(connection);
        org.h2gis.network.functions.NetworkFunctions.load(connection);
    }
    
    
}
