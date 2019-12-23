package mjt.resdbc;

import android.util.Log;

public class FtsInfo {

    public static final String FTSORDER_ASCENDING = "ASC";
    public static final String FTSORDER_DESCENDING = "DESC";
    public static final int FTSTYPE_NOTFTS = -1;
    public static final int FTSTYPE_FTS3 = 0;
    public static final int FTSTYPE_FTS4 = 1;
    private static final String[] FTSTYPE_DESCRIPTIVE = new String[]{"FTS3","FTS4"};


    /* @Fts4(
            contentEntity = word.class,
            tokenizer = "x",
            tokenizerArgs = {"A","B","C"},
            languageId = "",
            matchInfo = FtsOptions.MatchInfo.FTS4,
            notIndexed = {},
            prefix = {},
            order = FtsOptions.Order.ASC
        ) */
    private Class mContentEntity = null;
    private String mTokenizer = null;
    private String[] mTokenizerArgs = null;
    private String mLanguageId = null;
    private String mMatchInfo = null;
    private String[] mNotIndexedColumns = null;
    private int[] mPrefix = null;
    private String mOrder;
    private int mFtsType;

    public FtsInfo(TableInfo ti){

        Log.d("FTSINFO","Checking for FTS table for table " + ti.getTableName() + "Module is " + ti.getVirtualTableModule());
        //if (!ti.isFTSTable()) return;
        mFtsType = FTSTYPE_NOTFTS;
        for (int i=0;i < FTSTYPE_DESCRIPTIVE.length;i++) {
            if (FTSTYPE_DESCRIPTIVE[i].equals(ti.getVirtualTableModule().toUpperCase())) {
                mFtsType = i;
                break;
            }
        }
        if (mFtsType <= FTSTYPE_NOTFTS) return;
        Log.d("FTSINFO","Table" + ti.getTableName() +
                " detected as being FTS type " + FTSTYPE_DESCRIPTIVE[mFtsType] +
                "\n\t SQL is " + ti.getSQL()
        );
    }



    public Class getmContentEntity() {
        return mContentEntity;
    }

    public void setmContentEntity(Class mContentEntity) {
        this.mContentEntity = mContentEntity;
    }

    public String getmTokenizer() {
        return mTokenizer;
    }

    public void setmTokenizer(String mTokenizer) {
        this.mTokenizer = mTokenizer;
    }

    public String[] getmTokenizerArgs() {
        return mTokenizerArgs;
    }

    public void setmTokenizerArgs(String[] mTokenizerArgs) {
        this.mTokenizerArgs = mTokenizerArgs;
    }

    public String getmLanguageId() {
        return mLanguageId;
    }

    public void setmLanguageId(String mLanguageId) {
        this.mLanguageId = mLanguageId;
    }

    public String getmMatchInfo() {
        return mMatchInfo;
    }

    public void setmMatchInfo(String mMatchInfo) {
        this.mMatchInfo = mMatchInfo;
    }

    public String[] getmNotIndexedColumns() {
        return mNotIndexedColumns;
    }

    public void setmNotIndexedColumns(String[] mNotIndexedColumns) {
        this.mNotIndexedColumns = mNotIndexedColumns;
    }

    public int[] getmPrefix() {
        return mPrefix;
    }

    public void setmPrefix(int[] mPrefix) {
        this.mPrefix = mPrefix;
    }

    public String getmOrder() {
        return mOrder;
    }

    public void setmOrder(String mOrder) {
        this.mOrder = mOrder;
    }

    public int getmFtsType() {
        return mFtsType;
    }

    public void setmFtsType(int mFtsType) {
        this.mFtsType = mFtsType;
    }
}
