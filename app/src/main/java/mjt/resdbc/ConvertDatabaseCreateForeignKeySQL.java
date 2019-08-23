package mjt.resdbc;

import static mjt.resdbc.ConvertDatabaseBuilderCommonUtils.getColumnNameToCode;
import static mjt.resdbc.RoomCodeCommonUtils.getParentTable;
import static mjt.resdbc.RoomCodeCommonUtils.swapEnclosersForRoom;

public class ConvertDatabaseCreateForeignKeySQL {

    private static final String FOREIGNKEYCLAUSESTART = " FOREIGN KEY (";
    private static final String FOREIGNKEYCLAUSEPARENTSTART = " REFERENCES ";
    private static final String FOREIGNKEYSONUPDATE = " ON UPDATE ";
    private static final String FOREIGNKEYSONDELETE = " ON DELETE ";
    private static final String FOREIGNKEYSDEFERRABLE = " DEFERRABLE INITIALLY DEFERRED";

    /**
     * Generate the Foreign Key clauses (TABLE level) for the foreign keys of a table
     * @param peadbi    The PreExistingAssetDBInspect object
     * @param ti        The TableInfo object
     * @return          The ForeignKey clauses
     */
    public static String getForeignKeyClauses(PreExistingFileDBInspect peadbi, TableInfo ti) {
        StringBuilder fkeys = new StringBuilder();
        for (ForeignKeyInfo fki: ti.getForeignKeyList()) {
            if (fkeys.length() > 0) {
                fkeys.append(" ");
            }
            TableInfo parentti = getParentTable(peadbi,fki.getParentTableName());
            String parentTableName = swapEnclosersForRoom(parentti.getEnclosedTableName());
            if (parentTableName.length() < 1) {
                parentTableName = parentti.getTableName();
            }
            fkeys.append(FOREIGNKEYCLAUSESTART);
            boolean afterFirst = false;
            for (String s: fki.getChildColumnNames()) {
                if (afterFirst) {
                    fkeys.append(",");
                }
                afterFirst = true;
                fkeys.append("`").append(getColumnNameToCode(s,ti)).append("`");
            }
            afterFirst = false;
            fkeys.append(") ").append(FOREIGNKEYCLAUSEPARENTSTART).append("`").append(parentTableName).append("`").append("(");
            for(String s: fki.getParentColumnNames()) {
                if (afterFirst) {
                    fkeys.append(",");
                }
                afterFirst = true;
                fkeys.append("`").append(getColumnNameToCode(s,parentti)).append("`");
            }
            fkeys.append(")");
            if (fki.getOnUpdate() > ForeignKeyInfo.ACTION_NOACTION) {
                fkeys.append(FOREIGNKEYSONUPDATE);
                fkeys.append(ForeignKeyInfo.ACTION_KEYWORDS[fki.getOnUpdate()]);
                fkeys.append(" ");
            }
            if (fki.getOnDelete() > ForeignKeyInfo.ACTION_NOACTION) {
                fkeys.append(FOREIGNKEYSONDELETE);
                fkeys.append(ForeignKeyInfo.ACTION_KEYWORDS[fki.getOnDelete()]);
            }
            if (fki.isDeferable()) {
                fkeys.append(FOREIGNKEYSDEFERRABLE);
            }
        }
        return fkeys.toString();
    }
}
