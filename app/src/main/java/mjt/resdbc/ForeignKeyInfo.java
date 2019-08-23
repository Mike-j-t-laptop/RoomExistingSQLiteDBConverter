package mjt.resdbc;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForeignKeyInfo implements java.io.Serializable{
    public static final int
            ACTION_NOACTION = 0,
            ACTION_RESTRICT =1,
            ACTION_SETNULL =2,
            ACTION_SETDEFAULT = 3,
            ACTION_CASCADE = 4;
    public static final String[] ACTION_KEYWORDS = new String[]{
            "NO ACTION",
            "RESTRICT",
            "SET NULL",
            "SET DEFAULT",
            "CASCADE"
    };
    public static final String FKLISTCOL_ID = "id";
    public static final String FKLISTCOL_SEQ = "seq";
    public static final String FKLISTCOL_TABLE = "table";
    public static final String FKLISTCOL_FROM = "from";
    public static final String FKLISTCOL_TO = "to";
    public static final String FKLISTCOL_ONUPDATE = "on_update";
    public static final String FKLISTCOL_ONDELETE = "on_delete";
    public static final String FKLISTCOL_MATCH = "match";

    private String mTableName;
    private String mParentTableName;
    private ArrayList<String> mChildColumnNames;
    private ArrayList<String> mParentColumnNames;
    private int mOnUpdate;
    private int mOnDelete;
    private boolean mDeferable;

    public ForeignKeyInfo() {
        this.mParentTableName = null;
        this.mChildColumnNames = new ArrayList<>();
        this.mParentColumnNames = new ArrayList<>();
    }

    public ForeignKeyInfo(String tableName, String parentTable, List<String> childColumn, List<String> parentColumn, int onUpdateAction, int onDeleteAction, boolean deferable) {
        this.mTableName = tableName;
        this.mParentTableName = parentTable;
        this.mChildColumnNames = new ArrayList<>(childColumn);
        this.mParentColumnNames = new ArrayList<>(parentColumn);
        this.mOnUpdate = onUpdateAction;
        this.mOnDelete = onDeleteAction;
        this.mDeferable = deferable;
    }

    public ForeignKeyInfo(String tableName,String parentTable, String[] childColumn, String[] parentColumn, int onUpdateAction, int onDeleteAction, boolean deferable) {
        this(tableName,parentTable,Arrays.asList(childColumn),Arrays.asList(parentColumn),onUpdateAction,onDeleteAction,deferable);
    }

    public ForeignKeyInfo(String tableName, String parentTable, String[] childColumn, String[] parentColumn) {
        this(tableName,parentTable,Arrays.asList(childColumn),Arrays.asList(parentColumn),0,0,false);
    }

    public String getTableName() {
        return mTableName;
    }

    public void setTableName(String tableName) {
        this.mTableName = tableName;
    }

    public String getParentTableName() {
        return mParentTableName;
    }

    public void setParentTableName(String parentTableName) {
        this.mParentTableName = parentTableName;
    }

    public ArrayList<String> getChildColumnNames() {
        return mChildColumnNames;
    }

    public void setChildColumnNames(ArrayList<String> childColumnNames) {
        this.mChildColumnNames = childColumnNames;
    }

    public void setChildColumnNames(String[] childColumnNames) {
        this.mChildColumnNames = new ArrayList<>(Arrays.asList(childColumnNames));
    }

    public ArrayList<String> getParentColumnNames() {
        return mParentColumnNames;
    }

    public void setParentColumnNames(ArrayList<String> parentColumnNames) {
        this.mParentColumnNames = parentColumnNames;
    }

    public void setParentColumnNames(String[] parentColumnNames) {
        this.mParentColumnNames = new ArrayList<>(Arrays.asList(parentColumnNames));
    }

    public int getOnDelete() {
        return mOnDelete;
    }

    public void setOnDelete(int onDelete) {
        this.mOnDelete = onDelete;
    }

    public String getOnDeleteAction() {
        return ACTION_KEYWORDS[this.mOnDelete];
    }

    public int getOnUpdate() {
        return mOnUpdate;
    }

    public void setOnUpdate(int onUpdate) {
        this.mOnUpdate = onUpdate;
    }

    public String getOnUpdateAction() {
        return  ACTION_KEYWORDS[this.mOnUpdate];
    }

    public void addChildColumnParentColumnPair(String childColumn, String parentColumn) {
        addChildColumn(childColumn);
        addParentColumn(parentColumn);
    }

    public void addChildColumn(String childColumn) {
        mChildColumnNames.add(childColumn);
    }

    public void addParentColumn(String parentColumn) {
        mParentColumnNames.add(parentColumn);
    }

    public static int getActionAsInt(String action) {
        int rv = 0;
        for (int i=0; i < ACTION_KEYWORDS.length; i++) {
            if (action.equals(ACTION_KEYWORDS[i] )) {
                rv = i;
                break;
            }
        }
        return rv;
    }

    public boolean isDeferable() {
        return mDeferable;
    }

    public void setDeferable(boolean deferable) {
        this.mDeferable = deferable;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("For Table = ").append(this.getTableName());
        sb.append("\n\tParent Table = ").append(this.getParentTableName());
        for (int i=0; i < this.mParentColumnNames.size(); i++) {
            sb.append("\n\tChild Column = ").append(mChildColumnNames.get(i));
            sb.append(" references Parent Column =" ).append(mParentColumnNames.get(i));
        }
        sb.append("\n\t ON Update ").append(this.getOnUpdateAction()).append("\n\t ON Delete ").append(this.getOnDeleteAction());
        sb.append("\n\t DEFERRABLE = ").append(String.valueOf(this.isDeferable()));
        return sb.toString();
    }
}
