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

package org.h2gis.drivers.dbf;

import org.h2.command.ddl.CreateTableData;
import org.h2.constant.ErrorCode;
import org.h2.engine.Session;
import org.h2.index.Index;
import org.h2.index.IndexType;
import org.h2.message.DbException;
import org.h2.result.Row;
import org.h2.table.IndexColumn;
import org.h2.table.TableBase;
import org.h2gis.drivers.dbf.internal.DBFDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A table linked to a SHP and DBF files.
 * @author Nicolas Fortin
 */
public class DBFTable extends TableBase {
    private DBFDriver dbfDriver;
    private Logger log = LoggerFactory.getLogger(DBFTable.class);
    private DBFTableIndex baseIndex;

    public DBFTable(DBFDriver driver, CreateTableData data) throws IOException {
        super(data);
        this.dbfDriver = driver;
    }
    public void init(Session session) {
        baseIndex = new DBFTableIndex(dbfDriver,this,this.getId());
    }
    @Override
    public void lock(Session session, boolean exclusive, boolean force) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close(Session session) {
        try {
            dbfDriver.close();
        } catch (IOException ex) {
            log.error("Error while closing the SHP driver",ex);
        }
    }

    @Override
    public void unlock(Session s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Index addIndex(Session session, String indexName, int indexId, IndexColumn[] cols, IndexType indexType, boolean create, String indexComment) {
        // Index not managed
        return null;
    }

    @Override
    public void removeRow(Session session, Row row) {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"removeRow in Shape files");
    }

    @Override
    public void truncate(Session session) {
        baseIndex.truncate(session);
    }

    @Override
    public void addRow(Session session, Row row) {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"addRow in Shape files");
    }

    @Override
    public void checkSupportAlter() {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"addRow in Shape files");
    }

    @Override
    public String getTableType() {
        return TableBase.EXTERNAL_TABLE_ENGINE;
    }

    @Override
    public Index getScanIndex(Session session) {
        return baseIndex;
    }

    @Override
    public Index getUniqueIndex() {
        return null;
    }

    @Override
    public ArrayList<Index> getIndexes() {
        return new ArrayList<Index>();
    }

    @Override
    public boolean isLockedExclusively() {
        return false;
    }

    @Override
    public long getMaxDataModificationId() {
        return 0;
    }

    @Override
    public boolean isDeterministic() {
        return true;
    }

    @Override
    public boolean canGetRowCount() {
        return true;
    }

    @Override
    public boolean canDrop() {
        return true;
    }

    @Override
    public long getRowCount(Session session) {
        return dbfDriver.getRowCount();
    }

    @Override
    public long getRowCountApproximation() {
        return dbfDriver.getRowCount();
    }

    @Override
    public long getDiskSpaceUsed() {
        return 0;
    }

    @Override
    public void checkRename() {
        //Nothing to check
    }
}
