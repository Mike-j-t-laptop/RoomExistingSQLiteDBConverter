package mjt.resdbc;

import androidx.annotation.NonNull;

public class ViewInfo implements java.io.Serializable{

    private String mViewName;
    private String mViewTable;
    private String mViewSQL;

    public ViewInfo(String viewName, String viewTable, String mViewSQL) {
        this.mViewName = viewName;
        this.mViewTable = viewTable;
        this.mViewSQL = mViewSQL;
    }

    public String getViewName() {
        return mViewName;
    }
    public String getViewTable() {
        return mViewTable;
    }
    public String getViewSQL() {
        return mViewSQL;
    }

    @NonNull
    @Override
    public String toString() {
        return mViewName + " using " + mViewSQL;
    }
}
