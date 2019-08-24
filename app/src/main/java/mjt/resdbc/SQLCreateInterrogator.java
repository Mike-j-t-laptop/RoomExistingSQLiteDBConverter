package mjt.resdbc;

import java.util.List;

public class SQLCreateInterrogator {

    public static final String COMMA_INSIDE = ",(?=[^()]*\\))";
    public static final String COMMA_INSIDE_REPLACE = ":COMMA";
    public static final String DOUBLESPACES = " " + " ";
    // Entity enclosers
    private static final String[] ENTITY_ENCLOSER_OPENERS = new String[]{
            "[",
            "`",
            "\"",
            "'"
    };
    private static final String[] ENTITY_ENCLOSER_CLOSERS = new String[]{
            "]",
            "`",
            "\"",
            "'"
    };

    public static String getEnclosedTableName(TableInfo ti) {
        String sql = removeDoubleSpaces(ti.getSQL().trim());
        sql.replace("IF NOT EXISTS","").replaceAll("CREATE TABLE","");
        return getEnclosedEntityName(sql,ti.getTableName());
    }

    public static ColumnInfo getColumnDefinitionLines(TableInfo ti, String column_name) {
        ColumnInfo rv = new ColumnInfo();
        String sqluc = ti.getSQL().toUpperCase().trim();
        if (!sqluc.contains(SQLiteConstants.KEYWORD_CREATE + " ")) return rv;
        if (!sqluc.contains(" " + SQLiteConstants.KEYWORD_TABLE + " ")) return rv;
        if ((!sqluc.contains("("))) return rv;
        //Reduce multiple spaces to single spaces
        sqluc = removeDoubleSpaces(sqluc);
        sqluc = sqluc.replaceFirst(SQLiteConstants.KEYWORD_CREATE + " ","");
        sqluc = sqluc.replaceFirst("TABLE ","");
        sqluc = sqluc.replaceFirst("IF NOT EXISTS ","")
                .replaceFirst(ti.getTableName()
                        .toUpperCase(),"")
                .trim();
        for (int i = 0; i < ENTITY_ENCLOSER_OPENERS.length; i++) {
            sqluc = sqluc.replace(ENTITY_ENCLOSER_OPENERS[i]+ ENTITY_ENCLOSER_CLOSERS[i],"");
        }
        sqluc = sqluc.replaceFirst("\\(","").trim();
        String lc = sqluc.substring(sqluc.length()-1);
        if (lc.equals(")")) {
            sqluc = sqluc.substring(0,sqluc.length()-1);
        }
        sqluc = sqluc.replaceAll(COMMA_INSIDE,COMMA_INSIDE_REPLACE);
        String[] definitions = sqluc.split(",");
        for (String s: definitions) {
            s = s.replace(COMMA_INSIDE_REPLACE,",").replace("\n","").replace("\r","").replace("\t","");
            String altColumnName = getEnclosedEntityName(s,column_name);
            String columnNameToCheck = column_name;
            if ( altColumnName.length() > 0) {
                columnNameToCheck = altColumnName;
                rv.setAlternativeColumnName(altColumnName);
            }

            if (s.contains(column_name.toUpperCase())) {
                rv.setColumnName(column_name);
                rv.setUnique(s.contains(" UNIQUE "));
                rv.setNotNull(s.contains(" NOT NULL"));
                rv.setDefaultValue("");
                if (s.contains(" DEFAULT")) {
                    rv.setDefaultValue("supplied");
                }
                s = s.replaceAll(column_name.toUpperCase(),column_name);
                rv.setColumnCreateSQL(s);
                break;
            }
        }
        return rv;
    }

    private static String getEnclosedEntityName(String sql, String entityName) {
        for (int i = 0; i < ENTITY_ENCLOSER_OPENERS.length; i++) {
            String enclosedEntityName = extractEnclosedName(
                    sql,
                    ENTITY_ENCLOSER_OPENERS[i],
                    ENTITY_ENCLOSER_CLOSERS[i],
                    entityName
            );
            if (!enclosedEntityName.equals(entityName)) {
                //Never use double quotes so replace with `
                return enclosedEntityName.replace(
                        ENTITY_ENCLOSER_OPENERS[2],
                        ENTITY_ENCLOSER_OPENERS[1]
                );
            }
        }
        return "";
    }

    private static String extractEnclosedName(String sql, String startEncloser, String endEncloser, String originalName) {
        int startEncloserPosition, endEncloserPosition;
        startEncloserPosition = sql.indexOf(startEncloser);
        if (startEncloserPosition < 0) return originalName;
        endEncloserPosition = (sql.substring(startEncloserPosition+1).indexOf(endEncloser));
        if (endEncloserPosition < 0) return originalName;
        endEncloserPosition = endEncloserPosition + startEncloserPosition + 1;
        String extractedColumnName = sql.substring(startEncloserPosition+1,endEncloserPosition);
        if (extractedColumnName.toUpperCase().equals(originalName.toUpperCase())) {
            return startEncloser+originalName+endEncloser;
        }
        return originalName;
    }

    public static boolean isColumnDeferrable(TableInfo ti, List<String> foreignKeyColumnNames) {

        String sqluc = ti.getSQL().toUpperCase().trim();
        if (!sqluc.contains(SQLiteConstants.KEYWORD_DEFERRABLE)) return false;
        if (!sqluc.contains(" " + SQLiteConstants.KEYWORD_INITIALLY + " ")) return false;
        if (!sqluc.contains(" " + SQLiteConstants.KEYWORD_DEFERRED)) return false;
        if (!sqluc.contains(SQLiteConstants.KEYWORD_CREATE + " ")) return false;
        if (!sqluc.contains(" " + SQLiteConstants.KEYWORD_TABLE + " ")) return false;
        if ((!sqluc.contains("("))) return false;
        if (!sqluc.startsWith(SQLiteConstants.KEYWORD_CREATE + " ")) return false;
        sqluc = removeDoubleSpaces(sqluc);
        sqluc = sqluc.replaceFirst(SQLiteConstants.KEYWORD_CREATE + " ","");
        sqluc = sqluc.replaceFirst("TABLE ","");
        sqluc = sqluc.replaceFirst("IF NOT EXISTS ","").replaceFirst(ti.getTableName().toUpperCase(),"").trim();
        sqluc = sqluc.replaceFirst("\\(","").trim();
        String lc = sqluc.substring(sqluc.length()-1);
        if (lc.equals(")")) {
            sqluc = sqluc.substring(0,sqluc.length()-1);
        }
        sqluc = sqluc.replaceAll(COMMA_INSIDE,COMMA_INSIDE_REPLACE);
        String[] definitions = sqluc.split(",");
        for (String s: definitions) {
            if (s.indexOf(SQLiteConstants.KEYWORD_DEFERRABLE) < 1) continue;
            if (s.indexOf(SQLiteConstants.KEYWORD_INITIALLY) < 1) continue;
            if (s.indexOf(SQLiteConstants.KEYWORD_DEFERRED) < 1) continue;
            s = s.replace(COMMA_INSIDE_REPLACE,",");
            int matched_columns = 0;
            for (int i=0; i < foreignKeyColumnNames.size(); i++) {
                if (s.indexOf(foreignKeyColumnNames.get(i).toUpperCase()) > 0) {
                    matched_columns++;
                }
            }
            //Log.d(TAG,"Matched columns is " + String.valueOf(matched_columns) + " expected to match " + String.valueOf(foreignKeyColumnNames.size()));
            if (matched_columns == foreignKeyColumnNames.size()) {
                return true;
            }
        }
        return false;
    }

    public static String getIndexWhereExpression(String sql) {
        String rv = "";
        sql = removeDoubleSpaces(sql.trim());
        int wi = -1;
        if ((wi = sql.toUpperCase().indexOf(SQLiteConstants.KEYWORD_WHERE)) > 0) {
            rv = sql.substring(wi+1).replace(";","").trim();
        }
        return rv;
    }

    public static boolean isIndexUnique(String sql) {
        boolean rv = false;
        sql = removeDoubleSpaces(sql.replace(SQLiteConstants.KEYWORD_CREATE,"").trim());
        if (sql.substring(0,SQLiteConstants.KEYWORD_INDEX.length()).toUpperCase().equals(SQLiteConstants.KEYWORD_UNIQUE + " ")) {
            rv = true;
        }
        return rv;
    }

    public static String removeDoubleSpaces(String s) {
        while (s.indexOf(DOUBLESPACES) > 0) {
            s= s.replaceAll(DOUBLESPACES," ");
        }
        return s;
    }
}
