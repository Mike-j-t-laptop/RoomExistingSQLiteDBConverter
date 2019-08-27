package mjt.resdbc;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class RetrieveDBFiles {

    private static ArrayList<FileEntry> dbfiles;

    public static ArrayList<FileEntry> getFiles() {
        dbfiles = new ArrayList<>();
        createConverterBaseDirectories();
        getFiles(Environment.getExternalStoragePublicDirectory("").getPath());
        return dbfiles;
    }

    private static void getFiles(String directory) {

        File currentDirectory = new File(directory);
        if (currentDirectory.isFile()) {
            return;
        }
        //Log.d("GETFILES","Checking Directory " + currentDirectory.getPath());
        File[] listedFiles = currentDirectory.listFiles();
        if (listedFiles == null) {
            //Log.d("GETFILES","Skipping Empty Directory" + currentDirectory.getPath());
            return;
        }
        for (File f : listedFiles) {
            if (f.isDirectory()) {
                boolean skip = false;
                for (String s: new String[]{MainActivity.BASECONVERTDIRECTORY}) {
                    if(f.getName().equals(s)) {
                        skip = true;
                        //Log.d("GETFILES","Skipping Excluded Directory");
                        break;
                    }
                }
                if (!skip) getFiles(f.getPath());
            } else {
                //Log.d("GETFILES","Checking File "+ f.getPath());
                if (isFileSQLiteDatabase(f)) {
                    //Log.d("GETFILES", "SQLITEDATABASE FOUND AT " + f.getPath());
                    dbfiles.add(new FileEntry(f));
                }
            }
        }
    }

    public static void createConverterBaseDirectories() {
        File DBConvertDirectory = Environment.getExternalStoragePublicDirectory(MainActivity.BASECONVERTDIRECTORY);
        if (!DBConvertDirectory.exists()) {
            DBConvertDirectory.mkdirs();
        }
    }

    private static boolean isFileSQLiteDatabase(File f) {
        InputStream fis;
        if (!f.isFile()) return false;
        byte[] header = new byte[16];
        try {
            fis = new FileInputStream(f);
            fis.read(header);
            if (!new String(header).equals(SQLiteConstants.SQLITEFILEHEADER)) {
                fis.close();
                return false;
            }

        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
