package mjt.resdbc;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static mjt.resdbc.RoomCodeCommonUtils.capitalise;
import static mjt.resdbc.RoomDaoBuilder.DAOEXTENSION;
import static mjt.resdbc.RoomDaoBuilder.extractDaoCode;
import static mjt.resdbc.RoomEntityBuilder.extractEntityCode;

public class ConvertPreExistingDatabaseToRoom {

    public static final int MESSAGELEVEL_INFO = 0;
    public static final int MESSAGELEVEL_WARNING = 1;
    public static final int MESSAGELEVEL_ERROR = 3;
    private static final String[] sMessageType = new String[]{"INFO    -","WARNING -","ERROR   -"};
    private static String sConversionDirectory = MainActivity.BASECONVERTDIRECTORY + File.separator + "Convert_";
    private static String sEntityCodeSubDirectory = "RoomEntities";
    private static String sDAOCodeSubDirectory = "RoomDao";
    private static int sLoggingLevel = MESSAGELEVEL_ERROR;
    private static File ecsd, cd, daocd;
    private static final String TAG = "CNVPEADBI";
    private static final String INDENT = "\t";
    private static final int RESULTCODE_NOTHINGDONE = -9999;
    private static long totalOriginalRows, tor, totalCopiedRows, tcr;


    private static ArrayList<Message> sMessages;

    public static int Convert(PreExistingFileDBInspect peadbi, String conversionDirectory, String entityCodeSubDirectory, String daoCodeSubDirectory, int logging_level, String encloserStart, String encloserEnd) {
        sLoggingLevel = logging_level;
        int resultcode = RESULTCODE_NOTHINGDONE;
        sMessages = new ArrayList<>();
        if (conversionDirectory != null && conversionDirectory.length() > 0) {
            sConversionDirectory = conversionDirectory;
        }
        if (entityCodeSubDirectory != null && entityCodeSubDirectory.length() > 0) {
            sEntityCodeSubDirectory = entityCodeSubDirectory;
        }
        if (daoCodeSubDirectory != null && daoCodeSubDirectory.length() > 0) {
            sDAOCodeSubDirectory = daoCodeSubDirectory;
        }
        totalOriginalRows = 0;
        tor = 0;
        totalCopiedRows = 0;
        tcr = 0;
        if (!buildConversionDirectories()) {
            addMessage(new Message(1,MESSAGELEVEL_ERROR,"Unable to create conversion directories"));
            return resultcode;
        } else {
            addMessage(new Message(2,MESSAGELEVEL_INFO,"Conversion Directories Built for " + peadbi.getDatabaseName()));
        }
        resultcode--;
        int rc = buildEntityFiles(peadbi,encloserStart,encloserEnd);
        if (rc < 0) {
            addMessage(new Message(3,MESSAGELEVEL_ERROR,"Error Building Entity Files (JAVA code for ROOM Entities (Tables))"));
            return resultcode;
        } else {
            if (rc == peadbi.getTableCount()) {
                addMessage(new Message(4, MESSAGELEVEL_INFO,
                        "Entity Code Successfully Built for " + String.valueOf(peadbi.getTableCount()) +
                                " Java Classes in\n\t" + ecsd.getPath())
                );
            } else {
                addMessage( new Message(10,MESSAGELEVEL_INFO,
                        String.valueOf(rc) + " Entities built and " +
                        String.valueOf(peadbi.getTableCount() - rc) +" Entities skipped (FTS tables)"
                        )
                );
            }
        }
        resultcode--;
        rc = buildDaoFiles(peadbi, encloserStart,encloserEnd);
        if (rc < 0) {
            addMessage(new Message(5,MESSAGELEVEL_ERROR,"Error Building Dao Files (JAVA code for Data Acccess)"));
            return resultcode;
        } else {
            if (rc == peadbi.getTableCount()) {
                addMessage(new Message(6, MESSAGELEVEL_INFO,
                        "Dao Code Successfully Built for " + String.valueOf(peadbi.getTableCount()) +
                                " Java Classes in\n\t" + daocd.getPath())
                );
            } else {
                addMessage(new Message(11,MESSAGELEVEL_INFO,
                        String.valueOf(rc) + " Daos Built and " +
                        String.valueOf(peadbi.getTableCount() - rc) + " Daos Skipped (FTS Tables)"));
            }
        }
        resultcode--;
        if (!createConvertedDatabase(peadbi, encloserStart,encloserEnd)){
            addMessage( new Message(7,MESSAGELEVEL_ERROR,"Error(s) converting the Database from " + peadbi.getDatabasePath() + " to " + cd.getPath()));
            return resultcode;
        } else {
            addMessage(new Message(8,MESSAGELEVEL_INFO,
                    "Successfully converted the Database, " +
                            "which is located at \n\t" + cd.getPath())
            );
        }
        return 0;
    }

    public static int Convert(PreExistingFileDBInspect peadbi, String encloserStart, String encloserEnd) {
        return Convert(peadbi,null,null,null,MESSAGELEVEL_WARNING, encloserStart, encloserEnd);
    }

    public static int DebugConvert(PreExistingFileDBInspect peadbi, String encloserStart, String encloserEnd) {
        return Convert(peadbi,null,null,null,MESSAGELEVEL_INFO, encloserStart, encloserEnd);
    }

    public static int DebugConvert(PreExistingFileDBInspect peadbi, String conversionDirectory, String entitySubDirectory, String daoSubDirectory, String encloserStart, String encloserEnd) {
        return Convert(peadbi, conversionDirectory, entitySubDirectory,daoSubDirectory, MESSAGELEVEL_INFO, encloserStart, encloserEnd);
    }

    /**
     * create the Conversion Directory and the entities sub-directory
     * @return false if after making the directories they don no exist, true if OK.
     */
    private static boolean buildConversionDirectories() {
        ecsd = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + sConversionDirectory + File.separator + sEntityCodeSubDirectory);
        daocd = new File(ecsd.getParent() + File.separator + sDAOCodeSubDirectory);
        ecsd.delete();
        if (!ecsd.exists()) {
            ecsd.mkdirs();
        }
        daocd.delete();
        if (!daocd.exists()) {
            daocd.mkdirs();
        }
        cd = ecsd.getParentFile();
        return (cd.exists() && ecsd.exists() && daocd.exists());
    }

    /**
     * Build the Entity Files
     * @param peadbi    The PreExistingAssetDatabaseInspect object (i.e. all the database information)
     * @return          false if an io-error, else true
     */
    private static int buildEntityFiles(PreExistingFileDBInspect peadbi, String encloserStart, String encloserEnd) {
        int rc = 0;
        ArrayList<String> code;
        for (TableInfo ti: peadbi.getTableInfo()) {

            // Skip FTS tables
            if (ti.isFTSTable() && ti.getTableName().toLowerCase().contains("_fts_".toLowerCase())) {
                continue;
            }
            File currentEntity = new File(ecsd.getPath() + File.separator + capitalise(ti.getTableName()) + ".java");
            code = extractEntityCode(peadbi,ti, encloserStart,encloserEnd);
            try {
                FileWriter fw = new FileWriter(currentEntity);
                for (String s: code) {
                    fw.write(s+"\n");
                }
                fw.flush();
                fw.close();
                rc++;

            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return rc;
    }

    /**
     * Build basic DAO files
     * @param peadbi    The PreExisitingAssetDatabaseInspection object
     * @return          true if the files were successfully generated, else false
     */
    private static int buildDaoFiles(PreExistingFileDBInspect peadbi, String encloserStart, String EncloserEnd) {
        int rc = 0;
        ArrayList<String> code;
        for (TableInfo ti: peadbi.getTableInfo()) {
            if (ti.isFTSTable() && ti.getTableName().toLowerCase().contains("_fts_".toLowerCase())) {
                continue;
            }
            File currentDao = new File(daocd.getPath() + File.separator + capitalise(ti.getTableName()) + DAOEXTENSION + ".java");
            code = extractDaoCode(ti);
            try {
                FileWriter fw = new FileWriter(currentDao);
                for (String s: code) {
                    fw.write(s+"\n");
                }
                fw.flush();
                fw.close();
                rc++;
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return rc;
    }

    private static final String INSPECTDBATTACHNAME = "inspectdb";

    private static boolean createConvertedDatabase(PreExistingFileDBInspect peadbi, String encloserStart, String encloserEnd) {
        peadbi.closeInspectionDatabase();
        boolean rv = true;
        String TAG = "CRTCNVRTDB";
        String dbpath = cd.getPath() + File.separator + peadbi.getDatabaseName();
        File chkdb = new File(dbpath);
        if (chkdb.exists()) {
            chkdb.delete();
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbpath,null);
        // Disable Foreign Key Support to allow data to be loaded in any order
        db.setForeignKeyConstraintsEnabled(false);
        // Attach the original database copied from the assets
        db.execSQL("ATTACH DATABASE '" + peadbi.getDatabasePath() + "' AS " + INSPECTDBATTACHNAME);
        for (TableInfo ti: peadbi.getTableInfo()) {
            //Skip ROOM master table
            if (ti.getTableName().equals(SQLiteConstants.ROOM_MASTER_TABLE)) {
                addMessage(new Message(100,MESSAGELEVEL_WARNING,"ROOM's master table was skipped. Do you really want to convert a ROOM database?"));
                continue;
            }
            //Skip android_metadata table
            if (ti.getTableName().equals(SQLiteConstants.ANDROID_METADATA_TABLE)) {
                continue;
            }
            if (ti.isVirtualTable()) {
                if (!ti.isVirtualTableSupported()) {
                    addMessage(new Message(101, MESSAGELEVEL_WARNING, "VIRTUAL table was skipped (unsupported)."));
                }
                continue;
            }
            if (ti.isFTSTable()) {
                addMessage(new Message(107, MESSAGELEVEL_WARNING,"TABLE " + ti.getTableName() + " appears to be an FTS table and has been skipped"));
                continue;
            }

            String tableCreateSQL = GenerateTableSQL.generateTableSQL(ti,encloserStart,encloserEnd);
            try {
                Log.d(TAG,"Creating table " + ti.getTableName() + " using SQL as :-" + "\n\t" + tableCreateSQL);
                // Create the Tables
                db.execSQL(tableCreateSQL);


            } catch (SQLiteException e) {
                Log.d(TAG,"SQLite Error trying to create table " + ti.getTableName() +"\n\tError was " + e.getMessage());
                addMessage(new Message(101,MESSAGELEVEL_ERROR,"SQLite Error trying to create or load table " + ti.getTableName() + "\n\tError was " + e.getMessage() + "\n\t(check the log)"));
                e.printStackTrace();
                rv = false;
            }

            //String tableNameToCode = "main." + "`" + swapEnclosersForRoom(ti.getEnclosedTableName()) + "`";
            long originalRowCount = DatabaseUtils.queryNumEntries(db,INSPECTDBATTACHNAME + "." + ti.getTableName());
            totalOriginalRows = totalOriginalRows + originalRowCount;
            Cursor csr1 = db.query(INSPECTDBATTACHNAME + "." + encloserStart + ti.getTableName() + encloserEnd,new String[]{"count()"},null,null,null,null,null);
            if (csr1.moveToFirst()) {
                tor = tor + csr1.getLong(0);
            }
            csr1.close();
            String insertSQL = "INSERT OR IGNORE INTO main." + encloserStart + ti.getTableName()+  encloserEnd + " SELECT * FROM " + INSPECTDBATTACHNAME + "." + encloserStart + ti.getTableName() + encloserEnd + " WHERE 1";
            //String insertSQL = "INSERT INTO " + "`" + ti.getTableName() + "`" + " SELECT * FROM " + INSPECTDBATTACHNAME + "." + ti.getTableName() + " WHERE 1";
            try {
                db.execSQL(insertSQL);
                long convertedRowCount = DatabaseUtils.queryNumEntries(db,ti.getTableName());

                totalCopiedRows = totalCopiedRows + convertedRowCount;
                Cursor csr2 = db.query(ti.getTableName(),new String[]{"count()"},null,null,null,null,null);
                if (csr2.moveToFirst()) {
                    tcr = tcr +  csr2.getLong(0);
                }
                csr2.close();
                if (convertedRowCount < originalRowCount) {
                    addMessage(new Message(50,MESSAGELEVEL_WARNING,
                            "Some rows not inserted into table " + ti.getTableName() + "." +
                                    "\n\tThe original table had " + String.valueOf(originalRowCount) + "rows, " + String.valueOf(convertedRowCount) + " rows were inserted."
                    ));
                }
            } catch (SQLiteException e) {
                Log.d(TAG,"SQLite Error trying to insert data into " + ti.getTableName() +"\n\tError was " + e.getMessage() + "SQL was " + "\n\t" + insertSQL);
                addMessage(new Message(102,MESSAGELEVEL_ERROR,"SQLite Error trying to insert data into  " + ti.getTableName() + "SQL was " +
                        "\n\t" + insertSQL +
                        "\n\tError was " + e.getMessage() + "\n\t(check the log)"));
                e.printStackTrace();
                rv = false;
            }
        }
        for (TableInfo ti: peadbi.getTableInfo()) {
            if (ti.isVirtualTable() && ti.isVirtualTableSupported()) {
                //String tableCreateSQL = ConvertedDatabaseCreateVirtualTableSQL.createVirtualTableCreateSQL(peadbi,ti);
                String tableCreateSQL = GenerateTableSQL.generateVirtualTableSQL(ti,encloserStart,encloserEnd);
                Log.d(TAG,"Creating Virtual Table " + ti.getTableName() + " using SQL as \n\t" + tableCreateSQL);
                try {
                    db.execSQL(tableCreateSQL);
                } catch (SQLiteException e) {
                    Log.d(TAG,"SQLite Error trying to create  virtual table " + ti.getTableName() +"\n\tError was " + e.getMessage());
                    addMessage(new Message(103,MESSAGELEVEL_ERROR,"SQLite Error trying to create or load virtual table " + ti.getTableName() + "\n\tError was " + e.getMessage() + "\n\t(check the log)"));
                    e.printStackTrace();
                    rv = false;
                }
                String tableName = ti.getEnclosedTableName();
                if (tableName.length() < 1) {
                    tableName = ti.getTableName();
                }
                Cursor csr3 = db.query(tableName,null,null,null,null,null,null);
                int vtrowcount = csr3.getCount();
                if (vtrowcount < 1) {
                    String insertSQL = "INSERT OR IGNORE INTO main." + encloserStart + tableName + encloserEnd + " SELECT * FROM " + INSPECTDBATTACHNAME + "." + ti.getTableName() + " WHERE 1";
                    Log.d(TAG,"Populating Virtual Table " + ti.getTableName());
                    db.execSQL(insertSQL);
                }
                csr3 = db.query(tableName,null,null,null,null,null,null);
                vtrowcount = csr3.getCount();
                csr3.close();
                Log.d(TAG,"Virtual Table " + ti.getTableName() + " has " + String.valueOf(vtrowcount) + " rows.");
            }
        }
        db.execSQL("DETACH " + INSPECTDBATTACHNAME);
        if (!rv) {
            db.close();
            return false;
        }

        try {
            for (String s: GenerateIndexSQL.generateIndexSQL(peadbi,encloserStart,encloserEnd)) {
                Log.d(TAG,"Creating INDEX using \n\t" + s);
                db.execSQL(s);
            }
        } catch (SQLiteException e) {
            Log.d(TAG,"SQLite Error trying to build indexes " + "\n\tError was " + e.getMessage());
            e.printStackTrace();
            addMessage(new Message(105,MESSAGELEVEL_ERROR,"SQLite Error building indexes.\n\tError was " + e.getMessage()));
            db.close();
            return false;
        }
        addMessage(new Message(10,MESSAGELEVEL_INFO,String.valueOf(peadbi.getIndexCount()) + " Indexes built successfully."));

        try {
            for (String s: GenerateTriggerSQL.generateTriggerSQL(peadbi,"`","`")) {
                Log.d(TAG,"Creating TRIGGER using \n\t" + s);
                db.execSQL(s);
            }
        } catch (SQLiteException e) {
            Log.d(TAG,"SQLite Error trying to build triggers " + "\n\tError was " + e.getMessage());
            addMessage(new Message(106,MESSAGELEVEL_ERROR,"SQLite Error building Triggers.\n\tError was " + e.getMessage()));
            e.printStackTrace();
            db.close();
            return false;
        }
        addMessage(new Message(11,MESSAGELEVEL_INFO,String.valueOf(peadbi.getTriggerCount()) + " Triggers built successfully"));
        db.close();
        addMessage(new Message(99,MESSAGELEVEL_INFO,"Conversion completed."));
        return true;
    }


    /**
     * Write all messages to the log (unused)
     */
    public static void logMessages() {
        for (Message m: sMessages) {
            Log.d(TAG,sMessageType[m.getmMsgLevel()] + String.valueOf(m.getmMsgNumber()) + " - " + m.getmMessageText());
        }
    }

    /**
     * Add a Message
     * @param m the message to add
     */
    private static void addMessage(Message m) {
        if (m.getmMsgLevel() <= sLoggingLevel) {
            sMessages.add(m);
        }
    }

    /**
     * get all Messages as a string
     * @return  all the messages as a String
     */
    public static String getMessagesAsString() {
        StringBuilder msg = new StringBuilder();
        for (Message m: sMessages) {
            if (msg.length() > 0) {
                msg.append("\n");
            }
            switch (m.getmMsgLevel()) {
                case MESSAGELEVEL_INFO:
                    msg.append("        - ");
                    break;
                case MESSAGELEVEL_WARNING:
                    msg.append("WARNING - ");
                    break;
                case MESSAGELEVEL_ERROR:
                    msg.append("ERROR   - ");
            }
            msg.append(m.getmMessageText());
        }
        return msg.toString();
    }

    public static long getTotalOriginalRows() {
        return totalOriginalRows;
    }

    public static long getTotalCopiedRows() {
        return totalCopiedRows;
    }

    public static long getTor() {
        return tor;
    }

    public static long getTcr() {
        return tcr;
    }

    private static class Message {
        private int mMsgNumber;
        private int mMsgLevel;
        private String mMessageText;

        private Message(int msgNumber, int msgLevel, String msgText) {
            this.mMsgNumber = msgNumber;
            this.mMsgLevel = msgLevel;
            this.mMessageText = msgText;
        }

        public int getmMsgLevel() {
            return mMsgLevel;
        }

        public String getmMessageText() {
            return mMessageText;
        }

        public int getmMsgNumber() {
            return mMsgNumber;
        }
    }
}