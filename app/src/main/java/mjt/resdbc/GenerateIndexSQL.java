package mjt.resdbc;


import java.util.ArrayList;
import java.util.List;

public class GenerateIndexSQL {

    public static List<String> generateIndexSQL(PreExistingFileDBInspect pefdbi, String encloserStart, String encloserEnd) {
        ArrayList<String> indexSQLList = new ArrayList<>();
        for (IndexInfo ii: pefdbi.getIndexInfo()) {
            StringBuilder indexSQL = new StringBuilder();
            indexSQL.append(SQLiteConstants.KEYWORD_CREATE).append(" ");
            if (ii.isUnique()) {
                indexSQL.append(SQLiteConstants.KEYWORD_UNIQUE).append(" ");
            }
            indexSQL.append(SQLiteConstants.KEYWORD_INDEX).append(" ")
                    .append(encloserStart)
                    .append(ii.getIndexName())
                    .append(encloserEnd);
            indexSQL.append(" ").append(SQLiteConstants.KEYWORD_ON).append(" ")
                    .append(encloserStart).append(ii.getTableName()).append(encloserEnd).append(SQLiteConstants.GROUP_START);
            boolean afterfirst = false;
            for (IndexColumnInfo ici: ii.getColumns()) {
                if (afterfirst) {
                    indexSQL.append(",");
                }
                afterfirst = true;
                indexSQL.append(encloserStart).append(ici.getColumnName()).append(encloserEnd);
            }
            indexSQL.append(SQLiteConstants.GROUP_END);
            indexSQLList.add(indexSQL.toString());
        }
        return indexSQLList;
    }
}
