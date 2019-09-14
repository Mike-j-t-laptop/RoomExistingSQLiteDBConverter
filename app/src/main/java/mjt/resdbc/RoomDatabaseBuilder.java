package mjt.resdbc;

import java.util.ArrayList;

public class RoomDatabaseBuilder {

    public static ArrayList<String> extractDatabaseCode(PreExistingFileDBInspect pefdbi, String packageName) {
        ArrayList<String> tables = new ArrayList<>();
        ArrayList<String> databaseCode = new ArrayList<>();
        for (TableInfo ti: pefdbi.getTableInfo()) {
            if (ti.isVirtualTable() && ti.isVirtualTableSupported()) {
                tables.add(ti.getTableName());
            }
            if (!ti.isVirtualTable()) {
                tables.add(RoomCodeCommonUtils.capitalise(ti.getTableName().toLowerCase()));
            }
        }
        if (tables.size() < 1) {
            return databaseCode;
        }
        if (packageName != null && packageName.length() > 0) {
            databaseCode.add("package " + packageName + "\n\n");
        }
        databaseCode.add("@Database(version=1,entities = {\n");
        String lastTable = tables.get(tables.size()-1);
        String continuation = ",";
        for (String s: tables) {
            if (s.equals(lastTable)) {
                continuation = "";
            }
            databaseCode.add(RoomCodeCommonUtils.INDENT + s + ".class" + continuation + "\n");
        }
        databaseCode.add("})\n");
        databaseCode.add("public abstract class " + RoomCodeCommonUtils.capitalise(pefdbi.getDatabaseNameLessExtension()) + "Database extends RoomDatabase {\n\n");
        databaseCode.add(RoomCodeCommonUtils.INDENT + "public static final String DBNAME = \"" + pefdbi.getDatabaseName() + "\";\n\n");
        for (String s: tables) {
            databaseCode.add(RoomCodeCommonUtils.INDENT + "public abstract "+  s+"Dao get" + s + "Dao();\n");
        }
        databaseCode.add("}");

        return databaseCode;
    }
}
