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

package org.h2gis.drivers.shp;

import org.h2.command.ddl.CreateTableData;
import org.h2.constant.ErrorCode;
import org.h2.engine.Session;
import org.h2.index.BaseIndex;
import org.h2.index.Cursor;
import org.h2.index.IndexType;
import org.h2.message.DbException;
import org.h2.result.Row;
import org.h2.result.SearchRow;
import org.h2.result.SortOrder;
import org.h2.table.Column;
import org.h2.table.IndexColumn;
import org.h2.table.Table;
import org.h2.value.DataType;
import org.h2.value.Value;
import org.h2gis.drivers.dbf.DBFTableIndex;
import org.h2gis.drivers.dbf.internal.DbaseFileHeader;
import org.h2gis.drivers.shp.internal.SHPDriver;

import java.io.IOException;

/**
 * ScanIndex of SHPTable, the key is the row index.
 * @author Nicolas Fortin
 */
public class SHPTableIndex extends BaseIndex {
    SHPDriver driver;

    public SHPTableIndex(SHPDriver driver, Table table,int id) {
        this.driver = driver;
        IndexColumn indexColumn = new IndexColumn();
        indexColumn.columnName = "key";
        indexColumn.column = new Column("key",Value.LONG);
        initBaseIndex(table,id,table.getName()+"_DATA",new IndexColumn[] {indexColumn}, IndexType.createScan(true));
    }

    @Override
    public void checkRename() {
        // Nothing to check
    }

    @Override
    public Row getRow(Session session, long key) {
        try {
            Object[] row = driver.getRow(key);
            Value[] values = new Value[row.length];
            Column[] columns = table.getColumns();
            for(int idField=0;idField<row.length;idField++) {
                values[idField] = DataType.convertToValue(session, row[idField], columns[idField].getType());
            }
            return new Row(values, Row.MEMORY_CALCULATE);
        } catch (IOException ex) {
            throw DbException.get(ErrorCode.IO_EXCEPTION_1,ex);
        }
    }

    @Override
    public void close(Session session) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void add(Session session, Row row) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void remove(Session session, Row row) {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"remove in Shape files");
    }

    @Override
    public Cursor find(Session session, SearchRow first, SearchRow last) {
        return new SHPCursor(this,first != null ? first.getKey() : 0,session);
    }

    @Override
    public double getCost(Session session, int[] masks, SortOrder sortOrder) {
        return getRowCount(session);
    }

    @Override
    public void remove(Session session) {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"remove in Shape files");
    }

    @Override
    public void truncate(Session session) {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"truncate in Shape files");
    }

    @Override
    public boolean canGetFirstOrLast() {
        return true;
    }

    @Override
    public Cursor findFirstOrLast(Session session, boolean first) {
        return new SHPCursor(this,first ? 0 : getRowCount(session) - 1,session);
    }

    @Override
    public boolean needRebuild() {
        return false;
    }

    @Override
    public long getRowCount(Session session) {
        return driver.getRowCount();
    }

    @Override
    public long getRowCountApproximation() {
        return driver.getRowCount();
    }

    @Override
    public long getDiskSpaceUsed() {
        return 0;
    }

    /**
     * Parse the SHP and DBF files then init the provided data structure
     * @param data Data to initialise
     * @throws java.io.IOException
     */
    public static void feedCreateTableData(SHPDriver driver,CreateTableData data) throws IOException {
        if(data.columns.isEmpty()) {
            Column geometryColumn = new Column("THE_GEOM",Value.GEOMETRY);
            data.columns.add(geometryColumn);
            DBFTableIndex.feedCreateTableData(driver.getDbaseFileHeader(), data);
        }
    }

    private static class SHPCursor implements Cursor {
        private SHPTableIndex tIndex;
        private long rowIndex;
        private Session session;

        private SHPCursor(SHPTableIndex tIndex, long rowIndex, Session session) {
            this.tIndex = tIndex;
            this.rowIndex = rowIndex - 1;
            this.session = session;
        }

        @Override
        public Row get() {
            return tIndex.getRow(session, rowIndex);
        }

        @Override
        public SearchRow getSearchRow() {
            return get();
        }

        @Override
        public boolean next() {
            if(rowIndex + 1 < tIndex.getRowCount(session)) {
                rowIndex ++;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean previous() {
            if(rowIndex - 1 >= 0) {
                rowIndex --;
                return true;
            } else {
                return false;
            }
        }
    }
}
