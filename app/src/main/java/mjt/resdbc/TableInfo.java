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
        this.mVirtualTable = isVirtualTable(SQL);
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

    private boolean isWithoutRowid(String SQL) {
        return !SQL.replace(" ","").toUpperCase().equals(SQLiteConstants.SQLKEYWORD_WITHOUTROWID.replace(" ",""));
    }

    private boolean isVirtualTable(String SQL) {
        return SQLCreateInterrogator.removeDoubleSpaces(SQL).toUpperCase().contains(SQLiteConstants.CLAUSE_CREATEVIRTTBL);
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
