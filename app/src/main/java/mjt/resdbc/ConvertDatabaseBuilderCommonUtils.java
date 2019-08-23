package mjt.resdbc;

import static mjt.resdbc.RoomCodeCommonUtils.swapEnclosersForRoom;

public class ConvertDatabaseBuilderCommonUtils {

    public static final String UNIQUE = " UNIQUE ";


    /**
     * Get the column name to be used, that is the enclosed column name if there is one
     * @param columnName    The un-enclosed column name
     * @param ti            The TableInfo object for the table in which the clumn is located
     * @return              the column name,  enclosed if there is one, with enclosers swapped
     *                      to suit ROOM
     */
    public static String getColumnNameToCode(String columnName, TableInfo ti) {
        String rv = "";
        for (ColumnInfo ci: ti.getColumns()) {
            if (ci.getColumnName().equals(columnName)) {
                rv = ci.getAlternativeColumnName();
                if (rv.length() < 1) {
                    rv = ci.getColumnName();
                }
                return swapEnclosersForRoom(rv);
            }
        }
        return  rv;
    }
}
