package mjt.resdbc;

import java.util.ArrayList;

import static mjt.resdbc.RoomCodeCommonUtils.swapEnclosersForRoom;

public class ConvertDatabaseCreateTriggerSQL {

    /**
     * Build the Trigger Create SQL (ROOM does not support Triggers so they need to be built as part of the database to be imported)
     * @param peadbi    The PreExistinAssetDBInspect object
     * @return          The SQL to create the Trigger WARNING original SQL is used
     */
    public static ArrayList<String> buildTriggerCreateSQL(PreExistingFileDBInspect peadbi) {
        ArrayList<String> triggerCreateSQL = new ArrayList<>();
        for(TriggerInfo tri: peadbi.getTriggerInfo()) {
            String tableNameToCode = tri.getTriggerTable();
            String triggerNameToCode = swapEnclosersForRoom(tri.getTriggerName());
            TableInfo tableInfoToUse = null;
            for(TableInfo ti: peadbi.getTableInfo()) {
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
            triggerCreateSQL.add(tri.getTriggerSQL().replace(tri.getTriggerTable(),tableNameToCode).replace(tri.getTriggerName(),triggerNameToCode));
        }
        return triggerCreateSQL;
    }
}
