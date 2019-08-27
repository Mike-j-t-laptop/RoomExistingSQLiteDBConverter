package mjt.resdbc;

public class GenerateTableSQL {

    public static String generateTableSQL(TableInfo ti, String encloserStart, String encloserEnd) {
        StringBuilder tableSQL = new StringBuilder();
        String columnDefinitions = GenerateColumnSQL.generateColumnSQL(ti, encloserStart, encloserEnd);
        String foreignKeyDefinitions = GenerateForeignKeySQL.generateForeignKeySQL(ti, encloserStart,encloserEnd);
        String primaryKeyDefinition = GeneratePrimaryKeySQL.generatePrimaryKeySQL(ti,encloserStart,encloserEnd);
        tableSQL.append(SQLiteConstants.KEYWORD_CREATE).append(" ")
                .append(SQLiteConstants.KEYWORD_TABLE).append(" ")
                .append(SQLiteConstants.CLAUSE_IFNOTEXISTS).append(" ");
        tableSQL.append(encloserStart).append(ti.getTableName()).append(encloserEnd);
        tableSQL.append(SQLiteConstants.GROUP_START);
        if (columnDefinitions.length() > 0) {
            tableSQL.append(columnDefinitions);
        }
        if (foreignKeyDefinitions.length() > 0) {
            tableSQL.append(foreignKeyDefinitions);
        }
        if (primaryKeyDefinition.length() > 0) {
            tableSQL.append(primaryKeyDefinition);
        }
        tableSQL.append(SQLiteConstants.GROUP_END);
        return tableSQL.toString();
    }

    public static final String generateVirtualTableSQL (TableInfo ti, String encloserStart, String encloserEnd) {
        StringBuilder virtTableSQL = new StringBuilder();
        virtTableSQL.append(SQLiteConstants.CLAUSE_CREATEVIRTTBL).append(" ").append(SQLiteConstants.CLAUSE_IFNOTEXISTS).append(" ");
        virtTableSQL.append(ti.getTableName());
        virtTableSQL.append(" ").append(SQLiteConstants.KEYWORD_USING).append(" ").append(ti.getVirtualTableModule());
        virtTableSQL.append(SQLiteConstants.GROUP_START).append(getVirtualTableArgs(ti));
        virtTableSQL.append(SQLiteConstants.GROUP_END);
        return virtTableSQL.toString();
    }

    private static String getVirtualTableArgs(TableInfo ti) {
        return ti.getSQL().substring(ti.getSQL().indexOf(SQLiteConstants.GROUP_START) + 1).replace(SQLiteConstants.GROUP_END,"");
    }
}
