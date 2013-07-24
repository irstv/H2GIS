/*
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

package org.h2gis.drivers.dbf.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Merge ShapeFileReader and DBFReader
 * @author Nicolas Fortin
 */
public class DBFDriver {
    private File dbfFile;
    private DbaseFileReader dbaseFileReader;

    public void initDriverFromFile(File dbfFile) throws IOException {
        // Read columns from files metadata
        this.dbfFile = dbfFile;
        FileInputStream fis = new FileInputStream(dbfFile);
        dbaseFileReader = new DbaseFileReader(fis.getChannel());
    }

    /**
     * @return The DBF file header
     */
    public DbaseFileHeader getDbaseFileHeader() {
        return dbaseFileReader.getHeader();
    }

    public void close() throws IOException {
        dbaseFileReader.close();
    }
    /**
     * @return Row count
     */
    public long getRowCount() {
        return dbaseFileReader.getRecordCount();
    }

    /**
     * @param rowId Row index
     * @return The row content
     * @throws java.io.IOException
     */
    public Object[] getRow(long rowId) throws IOException {
        final int fieldCount = dbaseFileReader.getFieldCount();
        Object[] values = new Object[fieldCount];
        for(int fieldId=0;fieldId<fieldCount;fieldId++) {
            values[fieldId] = dbaseFileReader.getFieldValue((int)rowId, fieldId);
        }
        return values;
    }
}
