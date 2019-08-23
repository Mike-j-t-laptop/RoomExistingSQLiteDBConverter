package mjt.resdbc;

import androidx.annotation.NonNull;

public class TriggerInfo implements java.io.Serializable {

    private String mTriggerName;
    private String mTriggerTable;
    private String mTriggerSQL;

    public TriggerInfo(String triggerName, String triggerTable, String triggerSQL) {
        this.mTriggerName = triggerName;
        this.mTriggerTable = triggerTable;
        this.mTriggerSQL = triggerSQL;
    }

    public String getTriggerName() {
        return mTriggerName;
    }
    public void setTriggerName(String triggerName) {
        this.mTriggerName = triggerName;
    }
    public String getTriggerTable() {
        return mTriggerTable;
    }
    public void setTriggerTable(String triggerTable) {
        this.mTriggerTable = triggerTable;
    }
    public String getTriggerSQL() {
        return mTriggerSQL;
    }
    public void setTriggerSQL(String triggerSQL) {
        this.mTriggerSQL = triggerSQL;
    }

    @NonNull
    @Override
    public String toString() {
        return mTriggerName + " ON " + mTriggerName + " using \n" + mTriggerSQL;
    }
}
