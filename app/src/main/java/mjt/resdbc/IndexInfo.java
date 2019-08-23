package mjt.resdbc;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class IndexInfo implements java.io.Serializable {
    private String mIndexName;
    private String mSQL;
    private boolean mUnique;
    private String mTableName;
    private ArrayList<IndexColumnInfo> mColumns;
    private String mWhereClause;

    public IndexInfo(){
        mColumns = new ArrayList<>();
    }


    public IndexInfo(String indexName, String SQL, boolean unique, String tableName, ArrayList<IndexColumnInfo> columns, String whereClause) {
        this.mIndexName = indexName;
        this.mUnique = unique;
        this.mTableName = tableName;
        mColumns = new ArrayList<>();
        if (columns != null) {
            mColumns = columns;
        }
        this.mWhereClause = whereClause;
        this.mSQL = SQL;
    }

    public IndexInfo(String indexName, String tableName, ArrayList<IndexColumnInfo> columns) {
        this(indexName,"",false,tableName,columns,"");
    }

    public IndexInfo(String indexName, boolean unique, String tableName, ArrayList<IndexColumnInfo> columns) {
        this(indexName,"",unique,tableName,columns,"");
    }

    public String getIndexName() {
        return mIndexName;
    }

    public void setIndexName(String indexName) {
        this.mIndexName = indexName;
    }

    public String getSQL() {
        return mSQL;
    }

    public void setSQL(String SQL) {
        this.mSQL = SQL;
        if (SQL.toUpperCase().contains(" UNIQUE")) {
            this.setUnique(true);
        }
    }

    public boolean isUnique() {
        return mUnique;
    }

    public void setUnique(boolean unique) {
        this.mUnique = unique;
    }

    public String getTableName() {
        return mTableName;
    }

    public void setTableName(String tableName) {
        this.mTableName = tableName;
    }

    public ArrayList<IndexColumnInfo> getColumns() {
        return mColumns;
    }

    public void setColumns(ArrayList<IndexColumnInfo> columns) {
        this.mColumns = columns;
    }

    public String getWhereClause() {
        return mWhereClause;
    }

    public void setWhereClause(String whereClause) {
        this.mWhereClause = whereClause;
    }

    public void addColumn(String column, int columnIndexRank, int columnTableRank) {
        this.mColumns.add(new IndexColumnInfo(column,columnIndexRank,columnTableRank));
    }

    public void addColumns(ArrayList<IndexColumnInfo> columns) {
        this.mColumns.addAll(columns);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (IndexColumnInfo c: mColumns) {
            if (sb.length()> 1) sb.append(",");
            sb.append(c.getColumnName()).append("(").append(String.valueOf(c.getColumnIndexRank())).append(")");
        }
        return mIndexName + " ON " + mTableName + " - " + sb.toString();
    }
}
