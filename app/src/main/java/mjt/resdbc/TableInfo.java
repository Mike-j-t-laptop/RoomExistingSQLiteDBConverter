package mjt.resdbc;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

public class TableInfo implements java.io.Serializable {
    private String mTableName;
    private String mEnclosedTableName;
    private String mSQL;
    private ArrayList<ColumnInfo> mColumnInfos;
    private HashMap<String,Integer> mColumnLookup;
    private ArrayList<ForeignKeyInfo> mForeignKeyList;
    private ArrayList<String> mPrimaryKeyList;
    private ArrayList<String> mPrimaryKeyListAlternativeNames;
    private int referencelevel, mIndexCount, mTriggerCount;
    private boolean mRowid;
    private boolean mRoomTable;
    private boolean mVirtualTable = false;
    private String mVirtualTableModule = "";
    private boolean mVirtualTableSupported = false;

    public TableInfo(String tablename,
                     String SQL,ArrayList<ColumnInfo> columnInfo,
                     HashMap<String,Integer> columnLookup,
                     ArrayList<ForeignKeyInfo> foreignKeyInfo,
                     ArrayList<String> primaryKeyList,
                     ArrayList<String> primaryKeyListAlternativeNames,
                     int referenceLevel,
                     int indexCount,
                     int triggerCount, boolean rowid, boolean roomTable) {

        this.mTableName = tablename;
        this.mSQL = SQL;
        this.mColumnInfos = columnInfo;
        this.mColumnLookup = columnLookup;
        this.mForeignKeyList = foreignKeyInfo;
        this.mPrimaryKeyList = primaryKeyList;
        this.mPrimaryKeyListAlternativeNames = primaryKeyListAlternativeNames;
        this.referencelevel = referenceLevel;
        this.mIndexCount = indexCount;
        this.mTriggerCount = triggerCount;
        this.mRowid = rowid;
        this.mRoomTable = roomTable;
        this.mEnclosedTableName = SQLCreateInterrogator.getEnclosedTableName(this);
        setVirtualTableAttributes();
    }

    public TableInfo(String tablename, String SQL) {
        this(tablename,
                SQL,
                new ArrayList<ColumnInfo>(),
                new HashMap<String, Integer>(),
                new ArrayList<ForeignKeyInfo>(),
                new ArrayList<String>(),
                new ArrayList<String>(),
                0,0,0,true,tablename.equals(SQLiteConstants.ROOM_MASTER_TABLE)
        );
    }


    public String getTableName() {
        return mTableName;
    }

    public void setTableName(String mTableName) {
        this.mTableName = mTableName;
    }

    public String getEnclosedTableName() {
        return mEnclosedTableName;
    }

    public void setEnclosedTableName(String enclosedTableName) {
        this.mEnclosedTableName = enclosedTableName;
    }

    public String getSQL() {
        return mSQL;
    }

    public void setSQL(String mSQL) {
        this.mSQL = mSQL;
    }

    public ArrayList<ColumnInfo> getColumns() {
        return mColumnInfos;
    }

    public void setColumns(ArrayList<ColumnInfo> mColumnInfos) {
        this.mColumnInfos = mColumnInfos;
    }

    public HashMap<String,Integer> getColumnLookup() {
        return mColumnLookup;
    }

    public void setColumnLookup(HashMap<String, Integer> columnLookup) {
        this.mColumnLookup = columnLookup;
    }

    public ArrayList<ForeignKeyInfo> getForeignKeyList() {
        return mForeignKeyList;
    }

    public void addColumn(ColumnInfo columnInfo) {
        this.mColumnInfos.add(columnInfo);
        if (!mColumnLookup.containsKey(columnInfo.getColumnName())) {
            mColumnLookup.put(columnInfo.getColumnName(), mColumnInfos.size()-1);
        }
    }

    public void addForeignKeyListEntry(ForeignKeyInfo foreignKeyInfo) {
        mForeignKeyList.add(foreignKeyInfo);
    }

    public boolean isRoomTable() {
        return mRoomTable;
    }

    public void setRoomTable(boolean roomTable) {
        this.mRoomTable = roomTable;
    }

    public boolean isVirtualTable() {return mVirtualTable; }

    public void setVirtualTable(boolean virtualTable) {
        this.mVirtualTable = virtualTable;
    }

    public int getIndexCount() {
        return mIndexCount;
    }

    public void setIndexCount(int indexCount) {
        this.mIndexCount = indexCount;
    }

    public int getTriggerCount() {
        return mTriggerCount;
    }

    public void setTriggerCount(int triggerCount) {
        this.mTriggerCount = triggerCount;
    }

    public void buildPrimaryKeyList() {
        mPrimaryKeyList = new ArrayList<>();
        mPrimaryKeyListAlternativeNames = new ArrayList<>();
        int pk_count = 0;
        for (ColumnInfo ci: mColumnInfos) {
            if (ci.getPrimaryKeyPosition() > 0) {
                pk_count++;
            }
        }
        for (int i=1;i <= pk_count; i++) {
            for (ColumnInfo ci: mColumnInfos) {
                if (ci.getPrimaryKeyPosition() == i) {
                    mPrimaryKeyList.add(ci.getColumnName());
                    mPrimaryKeyListAlternativeNames.add(ci.getAlternativeColumnName());
                    break;
                }
            }
        }
    }

    public int getReferencelevel() {
        return referencelevel;
    }

    public void setReferencelevel(int referencelevel) {
        this.referencelevel = referencelevel;
    }

    public String getVirtualTableModule() {
        return mVirtualTableModule;
    }

    public void setVirtualTableModule(String virtualTableModule) {
        this.mVirtualTableModule = virtualTableModule;
    }

    public boolean isVirtualTableSupported() {
        return mVirtualTableSupported;
    }

    public void setVirtualTableSupported(boolean virtualTableSupported) {
        this.mVirtualTableSupported = virtualTableSupported;
    }

    public ArrayList<String> getPrimaryKeyList() {
        return mPrimaryKeyList;
    }

    public ArrayList<String> getPrimaryKeyListAlternativeNames() {
        return mPrimaryKeyListAlternativeNames;
    }

    public void setPrimaryKeyList(ArrayList<String> primaryKeyList) {
        this.mPrimaryKeyList = primaryKeyList;
    }

    public void setPrimaryKeyListAlternativeNames(ArrayList<String> primaryKeyListAlternativeNames) {
        this.mPrimaryKeyListAlternativeNames = primaryKeyListAlternativeNames;
    }

    public void incrementReferenceLevel() {
        this.referencelevel++;
    }

    public boolean isRowid() {
        return mRowid;
    }

    public void setRowid(boolean rowid) {
        this.mRowid = rowid;
    }

    public boolean isFTSTable() {
        for (ColumnInfo ci: mColumnInfos) {
            if (
                    (ci.getColumnName().toUpperCase().equals("docid".toUpperCase())
                    || ci.getColumnName().toUpperCase().equals("blockid".toUpperCase())
                    || ci.getColumnName().toUpperCase().equals("start_block".toUpperCase()))
                    && mTableName.toUpperCase().contains("_fts".toUpperCase())
            ) {
                return true;
            }
        }
        return false;
    }

    private boolean isWithoutRowid(String SQL) {
        return !SQL.replace(" ","").toUpperCase().equals(SQLiteConstants.SQLKEYWORD_WITHOUTROWID.replace(" ",""));
    }



    private void setVirtualTableAttributes() {
        String baseUpperSQL = SQLCreateInterrogator.removeDoubleSpaces(mSQL).toUpperCase();
        if (baseUpperSQL.contains(SQLiteConstants.CLAUSE_CREATEVIRTTBL)) {
            mVirtualTable = true;
            mVirtualTableModule = "";
            String unendedModuleName =
                    baseUpperSQL.substring(
                            baseUpperSQL.indexOf(" " + SQLiteConstants.KEYWORD_USING + " ") + 2 + SQLiteConstants.KEYWORD_USING.length()
                    );
            mVirtualTableModule = unendedModuleName.substring(0,unendedModuleName.indexOf("(")).trim();
            mVirtualTableSupported = false;
            for (String s:SQLiteConstants.SUPPORTEDVIRTUALTABLEMODULES) {
                if (s.equals(mVirtualTableModule)) {
                    mVirtualTableSupported = true;
                    break;
                }
            }
        } else {
            mVirtualTable = false;
        }
    }

    public ColumnInfo getColumnInfoByName(String columnName) {
        for (ColumnInfo ci: this.getColumns()) {
            if (columnName.equals(ci.getColumnName())) {
                return ci;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return this.mTableName;
    }
}
