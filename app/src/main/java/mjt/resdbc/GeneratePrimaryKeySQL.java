package mjt.resdbc;

import java.util.ArrayList;

public class GeneratePrimaryKeySQL {

    public static String generatePrimaryKeySQL(TableInfo ti, String encloserStart, String encloserEnd ) {
        StringBuilder primaryKeySQL = new StringBuilder();
        for (ColumnInfo ci: ti.getColumns()) {
            if (ci.isRowidAlias() || ci.isAutoIncrementCoded()) return "";
        }
        primaryKeySQL.append(",").append(SQLiteConstants.CLAUSE_PRIMARYKEY).append(SQLiteConstants.GROUP_START);
        boolean afterfirst = false;
        ArrayList<String> columnsToUse = ti.getPrimaryKeyList();
        if (ti.getPrimaryKeyList().size() < 1) {
            columnsToUse.clear();
            for (ColumnInfo ci: ti.getColumns()) {
                columnsToUse.add(ci.getColumnName());
            }
        }
        for (String s: columnsToUse) {
            if (afterfirst) {
                primaryKeySQL.append(",");
            }
            afterfirst = true;
            primaryKeySQL.append(encloserStart).append(s).append(encloserEnd);
        }
        primaryKeySQL.append(SQLiteConstants.GROUP_END);
        return primaryKeySQL.toString();
    }
}
