package mjt.resdbc;

import static mjt.resdbc.ConvertDatabaseBuilderCommonUtils.UNIQUE;
import static mjt.resdbc.RoomCodeCommonUtils.isColumnPartForeignKeyChild;
import static mjt.resdbc.RoomCodeCommonUtils.swapEnclosersForRoom;

public class ConvertDatabaseCreateColumnDefineSQL {

    private static final String NOTNULL = " NOT NULL ";
    private static final String ROWIDALIAS = " PRIMARY KEY AUTOINCREMENT" + NOTNULL;

    /**
     * Generate the SQL for the column definition clauses,
     * note that Foreign Keys constraints are defined as table constraints
     * @param ti    The TableInfo object
     * @return      The SQL as a String
     */
    public static String getColumnDefineClauses(TableInfo ti) {
        StringBuilder columns = new StringBuilder();
        for (ColumnInfo ci: ti.getColumns()) {
            // Add a comma separator between columns (after the first)
            if (columns.length() > 0) {
                columns.append(",");
            }
            // Get the column name, the enclosed if there is one
            // swapping enclosers that are disalowed (` and ") by Room
            String columnToCode = swapEnclosersForRoom(ci.getAlternativeColumnName());
            if (columnToCode.length() < 1) {
                columnToCode = ci.getColumnName();
            }
            // Build the initial part of the definition column name and type affinity
            // Noting that type affinity is strictly INTEGER, TEXT, REAL or BLOB
            columns.append("`").append(columnToCode).append("`").append(" ").append(ci.getFinalTypeAffinity());
            // If the column is an Alias of the rowid include PRIMARY KEY AUTOINCREMENT NOT NULL
            // again Room is pretty strict and appears to required the inefficient AUTOINCREMENT
            if (ci.isRowidAlias() || ci.isAutoIncrementCoded()) {
                columns.append(ROWIDALIAS);
            }
            //Apply NOT NULL if the column has NOT NULL
            // or if the column is a Foreign Key child
            // or if the column is part of a primary key
            // but not if the column is an alias of the rowid column (as already coded )
            if (
                    (isColumnPartForeignKeyChild(ti,ci.getColumnName()) || ci.isNotNull() || (ci.getPrimaryKeyPosition() > 0))
                            &&
                            !(ci.isAutoIncrementCoded() || ci.isRowidAlias())
            ) {
                columns.append(NOTNULL);
            }
            // Apply UNIQUE constraint if needed
            if (ci.isUnique()) {
                columns.append(UNIQUE);
            }
        }
        return columns.toString();
    }
}
