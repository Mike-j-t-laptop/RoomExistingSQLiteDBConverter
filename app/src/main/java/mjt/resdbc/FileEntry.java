package mjt.resdbc;

import androidx.annotation.NonNull;

import java.io.File;

public class FileEntry {
    private File mFile;

    public FileEntry(File file) {
        mFile = file;
    }

    public File getmFile() {
        return mFile;
    }

    @NonNull
    @Override
    public String toString() {
        return mFile.getPath();
    }
}
