package mjt.resdbc;

public class GenerateColumnSQL {

    public static String generateColumnSQL(TableInfo ti, String encloserStart, String encloserEnd) {
        boolean notnullUsed = false;
        StringBuilder columnSQL = new StringBuilder();
        for (ColumnInfo ci: ti.getColumns()) {
            if (columnSQL.length() > 0) {
                columnSQL.append(",");
            }
            columnSQL.append(encloserStart).append(ci.getColumnName()).append(encloserEnd).append(" ");
            columnSQL.append(ci.getFinalTypeAffinity());
            if (ci.isRowidAlias() || ci.isAutoIncrementCoded()) {
                columnSQL.append(" ").append(SQLiteConstants.CLAUSE_AUTOINCREMENT);
            }
            if ((RoomCodeCommonUtils.isColumnPartForeignKeyChild(ti,ci.getColumnName())
                    || ci.isNotNull()
                    || ci.getPrimaryKeyPosition() > 0)
                    && !(ci.isAutoIncrementCoded() || ci.isRowidAlias())
            ) {
                columnSQL. append(" ").append(SQLiteConstants.KEYWORD_NOTNULL);
                notnullUsed = true;
            }
            if (ti.getPrimaryKeyList().size() < 1 && !notnullUsed) {
                columnSQL.append(" ").append(SQLiteConstants.KEYWORD_NOTNULL);
            }
            if(ci.isUnique()) {
                columnSQL.append(" ").append(SQLiteConstants.KEYWORD_UNIQUE);
            }
            if (ci.getDefaultValue() != null && ci.getDefaultValue().length() > 0 ) {
                columnSQL.append(" ")
                        .append(SQLiteConstants.KEYWORD_DEFAULT).append(" ")
                        .append(ci.getDefaultValue());
            }
        }
        return columnSQL.toString();
    }
}
