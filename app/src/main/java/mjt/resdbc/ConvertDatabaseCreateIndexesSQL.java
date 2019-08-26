package mjt.resdbc;

import java.util.ArrayList;

import static mjt.resdbc.ConvertDatabaseBuilderCommonUtils.UNIQUE;
import static mjt.resdbc.ConvertDatabaseBuilderCommonUtils.getColumnNameToCode;
import static mjt.resdbc.RoomCodeCommonUtils.swapEnclosersForRoom;

public class ConvertDatabaseCreateIndexesSQL {

    /**
     * Generate the SQL to build the indexes WARNING WHERE clause for partial index is copied as is
     */
    private static final String CREATEINDEXSTART = "CREATE ";
    private static final String CREATEINDEXINDEX = " INDEX IF NOT EXISTS ";
    public static ArrayList<String> buildIndexCreateSQL(PreExistingFileDBInspect peadbi) {
        ArrayList<String> indexCreateSQL = new ArrayList<>();
        for (IndexInfo ii: peadbi.getIndexInfo()) {
            StringBuilder idx = new StringBuilder().append(CREATEINDEXSTART);
            if (ii.isUnique()) {
                idx.append(UNIQUE);
            }
            idx.append(CREATEINDEXINDEX);
            idx.append(swapEnclosersForRoom(ii.getIndexName())).append(" ON ");
            String tableNameToCode = ii.getTableName();
            TableInfo tableInfoToUse = null;
            for (TableInfo ti: peadbi.getTableInfo()) {
                if (ti.getTableName().equals(tableNameToCode)) {
                    tableInfoToUse = ti;
                    if (tableInfoToUse.getEnclosedTableName().length() > 0) {
                        tableNameToCode = swapEnclosersForRoom(ti.getEnclosedTableName());
                    }
                    break;
                }
            }
            if (tableInfoToUse == null) {
                continue;
            }
            idx.append("`").append(tableNameToCode).append("`").append("(");
            boolean afterFirst = false;
            for (IndexColumnInfo ici: ii.getColumns()) {
                if (afterFirst) {
                    idx.append(",");
                }
                afterFirst = true;
                idx.append("`").append(getColumnNameToCode(ici.getColumnName(),tableInfoToUse)).append("`");
            }
            idx.append(") ");
            String whereClause = ii.getWhereClause();
            if (whereClause.length() > 0) {
                idx.append("WHERE ").append(whereClause);
            }
            indexCreateSQL.add(idx.toString());
        }
        return indexCreateSQL;
    }
}
