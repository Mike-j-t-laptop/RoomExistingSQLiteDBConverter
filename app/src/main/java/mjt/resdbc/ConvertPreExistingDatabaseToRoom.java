package mjt.resdbc;

import android.content.Context;
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

import static mjt.resdbc.ConvertDatabaseCreateIndexesSQL.buildIndexCreateSQL;
import static mjt.resdbc.ConvertDatabaseCreateTriggerSQL.buildTriggerCreateSQL;
import static mjt.resdbc.RoomCodeCommonUtils.capitalise;
import static mjt.resdbc.RoomCodeCommonUtils.swapEnclosersForRoom;
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

    public static int Convert(Context context, PreExistingFileDBInspect peadbi, String conversionDirectory, String entityCodeSubDirectory, String daoCodeSubDirectory, int logging_level) {
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
        if (!buildEntityFiles(peadbi)) {
            addMessage(new Message(3,MESSAGELEVEL_ERROR,"Error Building Entity Files (JAVA code for ROOM Entities (Tables))"));
            return resultcode;
        } else {
            addMessage(new Message(4,MESSAGELEVEL_INFO,
                    "Entity Code Successfully Built for " + String.valueOf(peadbi.getTableCount()) +
                            " Java Classes in\n\t" + ecsd.getPath())
            );
        }
        resultcode--;
        if (!buildDaoFiles(peadbi)) {
            addMessage(new Message(5,MESSAGELEVEL_ERROR,"Error Building Dao Files (JAVA code for Data Acccess)"));
            return resultcode;
        } else {
            addMessage(new Message(6,MESSAGELEVEL_INFO,
                    "Dao Code Successfully Built for " + String.valueOf(peadbi.getTableCount()) +
                            " Java Classes in\n\t" + daocd.getPath())
            );
        }
        resultcode--;
        if (!createConvertedDatabase(peadbi)){
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

    public static int Convert(Context context, PreExistingFileDBInspect peadbi) {
        return Convert(context, peadbi,null,null,null,MESSAGELEVEL_WARNING);
    }

    public static int DebugConvert(Context context, PreExistingFileDBInspect peadbi) {
        return Convert(context,peadbi,null,null,null,MESSAGELEVEL_INFO);
    }

    public static int DebugConvert(Context context, PreExistingFileDBInspect peadbi, String conversionDirectory, String entitySubDirectory, String daoSubDirectory) {
        return Convert(context, peadbi, conversionDirectory, entitySubDirectory,daoSubDirectory, MESSAGELEVEL_INFO);
    }

    /**
     * create the Conversion Directory and the entities sub-directory
     * @return false if after making the directories they don no exist, true if OK.
     */
    private static boolean buildConversionDirectories() {
        ecsd = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + sConversionDirectory + File.separator + sEntityCodeSubDirectory);
        daocd = new File(ecsd.getParent() + File.separator + sDAOCodeSubDirectory);
        if (!ecsd.exists()) {
            ecsd.mkdirs();
        }
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
    private static boolean buildEntityFiles(PreExistingFileDBInspect peadbi) {
        ArrayList<String> code;
        for (TableInfo ti: peadbi.getTableInfo()) {
            File currentEntity = new File(ecsd.getPath() + File.separator + capitalise(ti.getTableName()) + ".java");
            code = extractEntityCode(peadbi,ti);
            try {
                FileWriter fw = new FileWriter(currentEntity);
                for (String s: code) {
                    fw.write(s+"\n");
                }
                fw.flush();
                fw.close();

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }


    /**
     * Build basic DAO files
     * @param peadbi    The PreExisitingAssetDatabaseInspection object
     * @return          true if the files were successfully generated, else false
     */
    private static boolean buildDaoFiles(PreExistingFileDBInspect peadbi) {
        ArrayList<String> code;
        for (TableInfo ti: peadbi.getTableInfo()) {
            File currentDao = new File(daocd.getPath() + File.separator + capitalise(ti.getTableName()) + DAOEXTENSION + ".java");
            code = extractDaoCode(ti);
            try {
                FileWriter fw = new FileWriter(currentDao);
                for (String s: code) {
                    fw.write(s+"\n");
                }
                fw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private static final String INSPECTDBATTACHNAME = "inspectdb";

    private static boolean createConvertedDatabase(PreExistingFileDBInspect peadbi) {
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
                addMessage(new Message(101,MESSAGELEVEL_WARNING,"VIRTUAL table was skipped (unsupported)."));
                continue;
            }
            String tableNameToCode = swapEnclosersForRoom(ti.getEnclosedTableName());
            if (tableNameToCode.length() < 1) {
                tableNameToCode = swapEnclosersForRoom(ti.getTableName());
            }
            String tableCreateSQL = ConvertedDatabaseCreateTableSQL.createTableCreateSQL(peadbi,ti);

            try {
                Log.d(TAG,"Creating table " + ti.getTableName() + " using SQL as :-" + "\n\t" + tableCreateSQL);
                // Create the Tables
                db.execSQL(tableCreateSQL);


            } catch (SQLiteException e) {
                Log.d(TAG,"SQLite Error trying to create table " + ti.getTableName() +"\n\tError was " + e.getMessage());
                addMessage(new Message(101,MESSAGELEVEL_ERROR,"SQLite Error trying to create or load table " + tableNameToCode + "\n\tError was " + e.getMessage() + "\n\t(check the log)"));
                e.printStackTrace();
                rv = false;
            }

            //String tableNameToCode = "main." + "`" + swapEnclosersForRoom(ti.getEnclosedTableName()) + "`";
            long originalRowCount = DatabaseUtils.queryNumEntries(db,INSPECTDBATTACHNAME + "." + ti.getTableName());
            totalOriginalRows = totalOriginalRows + originalRowCount;
            Cursor csr1 = db.query(INSPECTDBATTACHNAME + "." + ti.getTableName(),new String[]{"count()"},null,null,null,null,null);
            if (csr1.moveToFirst()) {
                tor = tor + csr1.getLong(0);
            }
            csr1.close();
            String insertSQL = "INSERT OR IGNORE INTO main." + "`" + tableNameToCode+  "`" + " SELECT * FROM " + INSPECTDBATTACHNAME + "." + ti.getTableName() + " WHERE 1";
            //String insertSQL = "INSERT INTO " + "`" + ti.getTableName() + "`" + " SELECT * FROM " + INSPECTDBATTACHNAME + "." + ti.getTableName() + " WHERE 1";
            try {
                db.execSQL(insertSQL);
                long convertedRowCount = DatabaseUtils.queryNumEntries(db,tableNameToCode);

                totalCopiedRows = totalCopiedRows + convertedRowCount;
                Cursor csr2 = db.query(tableNameToCode,new String[]{"count()"},null,null,null,null,null);
                if (csr2.moveToFirst()) {
                    tcr = tcr +  csr2.getLong(0);
                }
                csr2.close();
                if (convertedRowCount < originalRowCount) {
                    addMessage(new Message(50,MESSAGELEVEL_WARNING,
                            "Some rows not inserted." +
                                    "\n\tThe original table had " + String.valueOf(originalRowCount) + "rows, " + String.valueOf(convertedRowCount) + " rows were inserted." +
                                    "\n\tTOR count via Cursor is " + String.valueOf(tor) +
                                    "\n\tTCR cound via Cursor is " + String.valueOf(tcr)
                    ));
                }
            } catch (SQLiteException e) {
                Log.d(TAG,"SQLite Error trying to insert data into " + tableNameToCode +"\n\tError was " + e.getMessage() + "SQL was " + "\n\t" + insertSQL);
                addMessage(new Message(102,MESSAGELEVEL_ERROR,"SQLite Error trying to insert data into  " + tableNameToCode + "SQL was " +
                        "\n\t" + insertSQL +
                        "\n\tError was " + e.getMessage() + "\n\t(check the log)"));
                e.printStackTrace();
                rv = false;
            }
        }
        db.execSQL("DETACH " + INSPECTDBATTACHNAME);
        if (!rv) {
            db.close();
            return false;
        }

        try {
            //Build Indexes
            for (String s: buildIndexCreateSQL(peadbi)) {
                db.execSQL(s);
            }
        } catch (SQLiteException e) {
            Log.d(TAG,"SQLite Error trying to build indexes " + "\n\tError was " + e.getMessage());
            e.printStackTrace();
            addMessage(new Message(102,MESSAGELEVEL_ERROR,"SQLite Error building indexes.\n\tError was " + e.getMessage()));
            db.close();
            return false;
        }
        addMessage(new Message(10,MESSAGELEVEL_INFO,String.valueOf(peadbi.getIndexCount()) + " Indexes built successfully."));

        try {
            for (String s: buildTriggerCreateSQL(peadbi)) {
                db.execSQL(s);
            }
        } catch (SQLiteException e) {
            Log.d(TAG,"SQLite Error trying to build triggers " + "\n\tError was " + e.getMessage());
            addMessage(new Message(103,MESSAGELEVEL_ERROR,"SQLite Error building Triggers.\n\tError was " + e.getMessage()));
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