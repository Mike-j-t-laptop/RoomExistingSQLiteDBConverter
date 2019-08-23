package mjt.resdbc;

public class RoomCodeCommonUtils {

    public static final String INDENT = "\t";


    /**
     * swap the Enclosing characters if they are " or ` to ", this because ROOM rejects
     * doublt quotes and grave-accents (single quote used as it is easier)
     * @param s     The string in which to swap the Enclosers
     * @return      The string with the enclosers swapped
     */
    public static String swapEnclosersForRoom(String s) {
        return s.replaceAll("\"","'").replaceAll("`","'");
    }

    /**
     * Capitalise the String (i.e. make first character upper case)
     * @param s     The string to capitalise
     * @return      The capitialised string
     */
    public static String capitalise(String s) {
        return s.toUpperCase().charAt(0) + s.substring(1);
    }

    /**
     * Lowerise the String (i.e. make the first character lowercase)
     * @param s     The string to lowerise
     * @return      The lowerised string
     */
    public static String lowerise(String s) {
        return s.toLowerCase().charAt(0) + s.substring(1);
    }

    /**
     * Retrieve the respective TableInfo object from the Tables in the PreExistingAssetDBInspect object
     *
     * @param peadbi        The PreExistingAssetDBInspect object to find the table in
     * @param tableName     The TableName to be located
     * @return              The TableInfo object
     */
    public static TableInfo getParentTable(PreExistingFileDBInspect peadbi, String tableName) {
        for(TableInfo ti: peadbi.getTableInfo()) {
            if (ti.getTableName().equals(tableName)) {
                return ti;
            }
        }
        return null;
    }

    public static boolean isColumnPartForeignKeyChild(TableInfo ti,  String columnName) {
        for (ForeignKeyInfo fki: ti.getForeignKeyList()) {
            for (String s: fki.getChildColumnNames()) {
                if (s.equals(columnName)) return true;
            }
        }
        return false;
    }
}