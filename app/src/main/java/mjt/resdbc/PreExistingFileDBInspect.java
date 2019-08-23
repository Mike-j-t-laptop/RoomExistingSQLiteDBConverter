package mjt.resdbc;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static mjt.resdbc.SQLiteConstants.ANDROID_METADATA_TABLE;
import static mjt.resdbc.SQLiteConstants.SQLITEINDEXINFO_COL_INDEXRANK;
import static mjt.resdbc.SQLiteConstants.SQLITEINDEXINFO_COL_TABLERANK;
import static mjt.resdbc.SQLiteConstants.SQLITEINDEXINFO_COl_NAME;
import static mjt.resdbc.SQLiteConstants.SQLITEINDEXINFO_TABLENAME;
import static mjt.resdbc.SQLiteConstants.SQLITEMASTERTYPE_INDEX;
import static mjt.resdbc.SQLiteConstants.SQLITEMASTERTYPE_TABLE;
import static mjt.resdbc.SQLiteConstants.SQLITEMASTERTYPE_TRIGGER;
import static mjt.resdbc.SQLiteConstants.SQLITEMASTERTYPE_VIEW;
import static mjt.resdbc.SQLiteConstants.SQLITEMASTER_COL_NAME;
import static mjt.resdbc.SQLiteConstants.SQLITEMASTER_COL_SQL;
import static mjt.resdbc.SQLiteConstants.SQLITEMASTER_COL_TBLNAME;
import static mjt.resdbc.SQLiteConstants.SQLITEMASTER_COL_TYPE;
import static mjt.resdbc.SQLiteConstants.SQLITEMASTER_TABLENAME;
import static mjt.resdbc.SQLiteConstants.SQLITESYSTEMTABLEPREFIX;
import static mjt.resdbc.SQLiteConstants.SQLITETABLEINFO_COL_CID;
import static mjt.resdbc.SQLiteConstants.SQLITETABLEINFO_COL_DFLTVALUE;
import static mjt.resdbc.SQLiteConstants.SQLITETABLEINFO_COL_NAME;
import static mjt.resdbc.SQLiteConstants.SQLITETABLEINFO_COL_NOTNULL;
import static mjt.resdbc.SQLiteConstants.SQLITETABLEINFO_COL_PRIMARYKEY;
import static mjt.resdbc.SQLiteConstants.SQLITETABLEINFO_COL_TYPE;
import static mjt.resdbc.SQLiteConstants.SQLITETABLEINFO_TABLENAME;


public class PreExistingFileDBInspect {

    private Context mContext;
    private String mDatabaseName;
    private String mDatabasePath;
    private long mDatabaseDiskSize;
    private long mDatabaseVersion;
    private String mInspectDBName;
    private SQLiteDatabase mInspectDB;
    private HashMap<String,Integer> mTableLookUp;
    private ArrayList<TableInfo> mTableInfo;
    private ArrayList<ForeignKeyInfo> mForeignKeyInfo;
    private ArrayList<IndexInfo> mIndexInfo;
    private ArrayList<TriggerInfo> mTriggerInfo;
    private ArrayList<ViewInfo> mViewInfo;

    public PreExistingFileDBInspect(Context context, File databaseFile) {

        this.mContext = context;
        this.mDatabaseName = databaseFile.getName();
        this.mDatabasePath = databaseFile.getPath();
        this.mDatabaseDiskSize = databaseFile.length();
        mInspectDB = SQLiteDatabase.openDatabase(mDatabasePath,null,SQLiteDatabase.OPEN_READWRITE);
        mDatabaseVersion = mInspectDB.getVersion();
        mTableLookUp = new HashMap<>();
        mTableInfo = new ArrayList<>();
        mForeignKeyInfo = new ArrayList<>();
        mIndexInfo = new ArrayList<>();
        mTriggerInfo = new ArrayList<>();
        mViewInfo = new ArrayList<>();
        buildTableInfo();
        for (TableInfo ti: mTableInfo) {
            if (ti.getForeignKeyList().size() > 0) {
                mForeignKeyInfo.addAll(ti.getForeignKeyList());
            }
        }
        buildIndexInfo();
        buildTriggerInfo();
        buildViewInfo();
        assignTableCounts();
        assignRowidAliasFlags();
    }


    public long getDatabaseVersion() {
        return mDatabaseVersion;
    }

    public String getDatabaseName() {
        return mDatabaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.mDatabaseName = databaseName;
    }

    public String getInspectDBName() {
        return mInspectDBName;
    }

    public void setInspectDBName(String inspectDBName) {
        this.mInspectDBName = inspectDBName;
    }

    public long getDatabaseDiskSize() {
        return mDatabaseDiskSize;
    }

    public String getDatabasePath() {return mDatabasePath; }

    public ArrayList<TableInfo> getTableInfo() {
        return mTableInfo;
    }
    public ArrayList<ColumnInfo> getColumnInfo() {
        ArrayList<ColumnInfo> rv = new ArrayList<>();
        for (TableInfo ti: mTableInfo) {
            rv.addAll(ti.getColumns());
        }
        return rv;
    }


    public ArrayList<IndexInfo> getIndexInfo() {
        return mIndexInfo;
    }

    public ArrayList<TriggerInfo> getTriggerInfo() {
        return mTriggerInfo;
    }

    public ArrayList<ViewInfo> getViewInfo() {
        return mViewInfo;
    }

    public int getTableCount() {
        return mTableInfo.size();
    }
    public int getColumnCount() {
        int rv = 0;
        for (TableInfo ti: mTableInfo ) {
            rv = rv + ti.getColumns().size();
        }
        return rv;
    }
    public int getIndexCount() { return mIndexInfo.size(); }
    public int getIndexColumnCount() {
        int rv = 0;
        for (IndexInfo ii: mIndexInfo) {
            rv = rv + ii.getColumns().size();
        }
        return rv;
    }
    public int getTriggerCount() {return mTriggerInfo.size(); }
    public int getViewCount() {return mViewInfo.size();}

    public ArrayList<ForeignKeyInfo> getForeignKeyInfo() { return mForeignKeyInfo;}

    public  int getForeignKeyCount() {
        int rv = 0;
        for (TableInfo ti: mTableInfo) {
            rv = rv + ti.getForeignKeyList().size();
        }
        return rv;
    }


    /**
     * Build the Table info for the database
     */
    private void buildTableInfo() {
        Cursor csr = mInspectDB.query(
                SQLITEMASTER_TABLENAME,
                null,
                SQLITEMASTER_COL_TYPE+"=?",new
                        String[]{SQLITEMASTERTYPE_TABLE},
                null,null,null
        );
        while (csr.moveToNext()) {
            String tableName = csr.getString(csr.getColumnIndex(SQLITEMASTER_COL_NAME));
            if (!tableName.startsWith(SQLITESYSTEMTABLEPREFIX) && !tableName.equals(ANDROID_METADATA_TABLE)) {
                TableInfo ti = new TableInfo(tableName,csr.getString(csr.getColumnIndex(SQLITEMASTER_COL_SQL)));
                mTableInfo.add(ti);
                if (!mTableLookUp.containsKey(ti.getTableName())) {
                    mTableLookUp.put(ti.getTableName(),mTableInfo.size()-1);
                }
                buildColumnInfo(mTableInfo.get(mTableInfo.size()-1));
                buildForeignKeyInfoList(ti);
                (mTableInfo.get(mTableInfo.size()-1)).buildPrimaryKeyList();
            }
        }
        csr.close();
    }

    private void buildColumnInfo(TableInfo ti) {
        Cursor csr = mInspectDB.rawQuery(
                "PRAGMA " +
                        SQLITETABLEINFO_TABLENAME + "('" + ti.getTableName() + "')",
                null
        );
        while (csr.moveToNext()) {
            String columnName = csr.getString(csr.getColumnIndex(SQLITETABLEINFO_COL_NAME));
            ColumnInfo ci = new ColumnInfo(
                    columnName,
                    ti.getTableName(),
                    csr.getString(csr.getColumnIndex(SQLITETABLEINFO_COL_TYPE)),
                    csr.getInt(csr.getColumnIndex(SQLITETABLEINFO_COL_NOTNULL))> 0,
                    csr.getInt(csr.getColumnIndex(SQLITETABLEINFO_COL_CID)),
                    csr.getInt(csr.getColumnIndex(SQLITETABLEINFO_COL_PRIMARYKEY)),
                    csr.getString(csr.getColumnIndex(SQLITETABLEINFO_COL_DFLTVALUE))
            );
            ColumnInfo extra_ci = SQLCreateInterrogator.getColumnDefinitionLines(ti,columnName);
            if (extra_ci != null && extra_ci.getColumnName().length() > 0) {
                ci.setAlternativeColumnName(extra_ci.getAlternativeColumnName());
                ci.setNotNull(extra_ci.isNotNull());
                ci.setUnique((extra_ci.isUnique()));
                ci.setRowidAlias(extra_ci.isRowidAlias());
                ci.setDefaultValue(extra_ci.getDefaultValue());
                ci.setColumnCreateSQL(extra_ci.getColumnCreateSQL());
            }
            ti.addColumn(ci);
        }
        csr.close();
    }

    private void buildIndexInfo() {

        Cursor csr = mInspectDB.query(
                SQLITEMASTER_TABLENAME,
                null,
                SQLITEMASTER_COL_TYPE+"=?",
                new String[]{SQLITEMASTERTYPE_INDEX},
                null,null,null
        );
        ArrayList<IndexColumnInfo> columns = new ArrayList<>();
        while (csr.moveToNext()) {
            String indexName = csr.getString(csr.getColumnIndex(SQLITEMASTER_COL_NAME));
            String indexSQL = csr.getString(csr.getColumnIndex(SQLITEMASTER_COL_SQL));
            if (!indexName.startsWith(SQLITESYSTEMTABLEPREFIX)) {
                IndexInfo ii = new IndexInfo();
                ii.setIndexName(indexName);
                ii.setTableName(csr.getString(csr.getColumnIndex(SQLITEMASTER_COL_TBLNAME)));
                ii.setWhereClause(SQLCreateInterrogator.getIndexWhereExpression(indexSQL));
                ii.setUnique(SQLCreateInterrogator.isIndexUnique(indexSQL));
                ii.setSQL(indexSQL);
                buildIndexColumnInfo(ii);
                mIndexInfo.add(ii);
            }
        }
        csr.close();
    }

    private void buildIndexColumnInfo(IndexInfo ii) {
        Cursor csr = mInspectDB.rawQuery(
                "PRAGMA " +
                        SQLITEINDEXINFO_TABLENAME + "('" + ii.getIndexName() +"')",
                null
        );
        while (csr.moveToNext()) {
            IndexColumnInfo ixci = new IndexColumnInfo(
                    csr.getString(
                            csr.getColumnIndex(SQLITEINDEXINFO_COl_NAME)),
                    csr.getInt(csr.getColumnIndex(SQLITEINDEXINFO_COL_INDEXRANK)),
                    csr.getInt(csr.getColumnIndex(SQLITEINDEXINFO_COL_TABLERANK))
            );
            ii.addColumn(ixci.getColumnName(),ixci.getColumnIndexRank(),ixci.getColumnTableRank());
        }
        csr.close();
    }

    private void buildTriggerInfo() {
        Cursor csr = mInspectDB.query(
                SQLITEMASTER_TABLENAME,
                null,
                SQLITEMASTER_COL_TYPE + "=?",
                new String[]{SQLITEMASTERTYPE_TRIGGER},
                null,null,null
        );
        while (csr.moveToNext()) {
            mTriggerInfo.add(
                    new TriggerInfo(
                            csr.getString(csr.getColumnIndex(SQLITEMASTER_COL_NAME)),
                            csr.getString(csr.getColumnIndex(SQLITEMASTER_COL_TBLNAME)),
                            csr.getString(csr.getColumnIndex(SQLITEMASTER_COL_SQL))
                    )
            );
        }
        csr.close();
    }

    private void buildViewInfo() {
        Cursor csr = mInspectDB.query(
                SQLITEMASTER_TABLENAME,
                null,
                SQLITEMASTER_COL_TYPE + "=?",
                new String[]{SQLITEMASTERTYPE_VIEW},
                null,null,null
        );
        while (csr.moveToNext()) {
            mViewInfo.add(
                    new ViewInfo(
                            csr.getString(csr.getColumnIndex(SQLITEMASTER_COL_NAME)),
                            csr.getString(csr.getColumnIndex(SQLITEMASTER_COL_TBLNAME)),
                            csr.getString(csr.getColumnIndex(SQLITEMASTER_COL_SQL))
                    )
            );
        }
        csr.close();
    }

    private void buildForeignKeyInfoList(TableInfo ti) {
        int currentFKID = -1;
        ForeignKeyInfo currentFKI = new ForeignKeyInfo();
        ArrayList<ForeignKeyInfo> fki = new ArrayList<>();
        Cursor csr = mInspectDB.rawQuery("PRAGMA foreign_key_list('" + ti.getTableName() + "')",null);
        while (csr.moveToNext()) {
            int retrievedFKID = csr.getInt(csr.getColumnIndex(ForeignKeyInfo.FKLISTCOL_ID));
            int retrievedFKSeq = csr.getInt(csr.getColumnIndex(ForeignKeyInfo.FKLISTCOL_SEQ));
            int retrievedOnUpdateAction = ForeignKeyInfo.getActionAsInt(csr.getString(csr.getColumnIndex(ForeignKeyInfo.FKLISTCOL_ONUPDATE)));
            int retrievedOnDeleteAction = ForeignKeyInfo.getActionAsInt(csr.getString(csr.getColumnIndex(ForeignKeyInfo.FKLISTCOL_ONDELETE)));
            String retrievedParentTableName = csr.getString(csr.getColumnIndex(ForeignKeyInfo.FKLISTCOL_TABLE));
            String retrievedChildColumnName = csr.getString(csr.getColumnIndex(ForeignKeyInfo.FKLISTCOL_FROM));
            String retrievedParentColumnName = csr.getString(csr.getColumnIndex(ForeignKeyInfo.FKLISTCOL_TO));
            if (retrievedFKID != currentFKID) {
                if (currentFKI.getParentTableName() != null) {
                    fki.add(currentFKI);
                }
                currentFKI = new ForeignKeyInfo();
                currentFKI.setTableName(ti.getTableName());
                currentFKI.setParentTableName(retrievedParentTableName);
                currentFKI.setOnUpdate(retrievedOnUpdateAction);
                currentFKI.setOnDelete(retrievedOnDeleteAction);
                currentFKI.addChildColumnParentColumnPair(retrievedChildColumnName,retrievedParentColumnName);
            } else {
                currentFKI.addChildColumnParentColumnPair(retrievedChildColumnName,retrievedParentColumnName);
            }
            if (SQLCreateInterrogator.isColumnDeferrable(ti,currentFKI.getChildColumnNames())) {
                currentFKI.setDeferable(true);
            }

            currentFKI.setDeferable(SQLCreateInterrogator.isColumnDeferrable(ti,currentFKI.getChildColumnNames()));
            currentFKID = retrievedFKID;
        }
        csr.close();
        if (currentFKID > -1) {
            fki.add(currentFKI);
        }
        for (ForeignKeyInfo f: fki) {
            ti.addForeignKeyListEntry(f);
        }

    }

    private void assignTableCounts() {
        for (TableInfo ti: mTableInfo) {
            ti.setIndexCount(0);
            ti.setTriggerCount(0);
            for (IndexInfo ii: this.getIndexInfo()) {
                if (ii.getTableName().toUpperCase().equals(ti.getTableName().toUpperCase())) {
                    ti.setIndexCount(ti.getIndexCount()+1);
                }
            }
            for (TriggerInfo tri: this.getTriggerInfo()) {
                if (tri.getTriggerTable().toUpperCase().equals(ti.getTableName().toUpperCase())) {
                    ti.setTriggerCount(ti.getTriggerCount()+1);
                }
            }
        }
    }

    private void assignRowidAliasFlags() {
        for (TableInfo ti: this.getTableInfo()) {
            if (ti.getPrimaryKeyList().size() == 1) {
                ColumnInfo ci = ti.getColumnInfoByName(ti.getPrimaryKeyList().get(0));
                String colsql = SQLCreateInterrogator.removeDoubleSpaces(ci.getColumnCreateSQL().toUpperCase().trim());
                if (ci != null) {
                    if (colsql.contains(" INTEGER ")) {
                        ci.setRowidAlias(true);
                        if (colsql.contains(" AUTOINCREMENT")) {
                            ci.setAutoIncrementCoded(true);
                        }
                        continue;
                    }
                }
            }
        }
    }

    /**
     * Close the Inspection database
     */
    public void closeInspectionDatabase() {
        mInspectDB.close();
    }
}
