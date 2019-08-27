package mjt.resdbc;

import android.text.method.QwertyKeyListener;

public class GenerateForeignKeySQL {

    public static String generateForeignKeySQL(TableInfo ti, String encloserStart, String encloserEnd) {
        StringBuilder fkeySQL = new StringBuilder();
        for (ForeignKeyInfo fki: ti.getForeignKeyList()) {
            //if (fkeySQL.length() > 0) {
                fkeySQL.append(",");
            //}
            fkeySQL.append(" ").append(SQLiteConstants.CLAUSE_FOREIGNKEY_START).append(SQLiteConstants.GROUP_START);
            boolean afterfirst = false;
            for (String s: fki.getChildColumnNames()) {
                if (afterfirst) {
                    fkeySQL.append(",");
                }
                afterfirst = true;
                fkeySQL.append(encloserStart).append(s).append(encloserEnd);
            }
            fkeySQL.append(SQLiteConstants.GROUP_END);
            fkeySQL.append(" ").append(SQLiteConstants.KEYWORD_REFERENCES).append(" ");
            fkeySQL.append(encloserStart).append(fki.getParentTableName()).append(encloserEnd);
            fkeySQL.append(SQLiteConstants.GROUP_START);
            afterfirst = false;
            for (String s: fki.getParentColumnNames()) {
                if (afterfirst) {
                    fkeySQL.append(",");
                }
                fkeySQL.append(encloserStart).append(s).append(encloserEnd);
            }
            fkeySQL.append(SQLiteConstants.GROUP_END);
            if (fki.getOnUpdate() > ForeignKeyInfo.ACTION_NOACTION) {
                fkeySQL.append(" ").append(SQLiteConstants.CLAUSE_FOREIGNKEY_ONUPDATE);
                fkeySQL.append(" ").append(ForeignKeyInfo.ACTION_KEYWORDS[fki.getOnUpdate()]);
            }
            if (fki.getOnDelete() > ForeignKeyInfo.ACTION_NOACTION) {
                fkeySQL.append(" ").append(SQLiteConstants.CLAUSE_FOREIGNKEY_ONDELETE);
                fkeySQL.append(" ").append(ForeignKeyInfo.ACTION_KEYWORDS[fki.getOnDelete()]);
            }
            if (fki.isDeferable()) {
                fkeySQL.append(" ").append(SQLiteConstants.CLAUSE_FOREIGNKEY_DEFERRABLE);
            }
        }
        return fkeySQL.toString();
    }
}
