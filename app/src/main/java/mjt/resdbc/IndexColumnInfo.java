package mjt.resdbc;

public class IndexColumnInfo implements java.io.Serializable{
    private String mColumnName;
    private int mColumnIndexRank;
    private int mColumnTableRank;

    public IndexColumnInfo(String columnName, int columnIndexRank, int columnTableRank) {
        this.mColumnName = columnName;
        this.mColumnIndexRank = columnIndexRank;
        this.mColumnTableRank = columnTableRank;
    }

    public String getColumnName() {
        return mColumnName;
    }
    public void setColumnName(String columnName) {
        this.mColumnName = columnName;
    }
    public int getColumnIndexRank() {
        return mColumnIndexRank;
    }
    public void setColumnIndexRank(int columnIndexRank) {
        this.mColumnIndexRank = columnIndexRank;
    }
    public int getColumnTableRank() {
        return mColumnTableRank;
    }
    public void setColumnTableRank(int columnTableRank) {
        this.mColumnTableRank = columnTableRank;
    }
}
