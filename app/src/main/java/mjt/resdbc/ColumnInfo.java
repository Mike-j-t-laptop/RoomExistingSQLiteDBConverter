package mjt.resdbc;

import androidx.annotation.NonNull;

import static mjt.resdbc.SQLiteConstants.AFFINITY_BLOB;
import static mjt.resdbc.SQLiteConstants.AFFINITY_INTEGER;
import static mjt.resdbc.SQLiteConstants.AFFINITY_NUMERIC;
import static mjt.resdbc.SQLiteConstants.AFFINITY_REAL;
import static mjt.resdbc.SQLiteConstants.AFFINITY_TEXT;

public class ColumnInfo implements java.io.Serializable {
    private String mOriginalColumnName = "";
    private String mOriginalEnclosedColumnName = "";
    private String mColumnName;
    private String mAlternativeColumnName = "";
    private String mOwningTable;
    private String mColumnType;
    private int mCID;
    private String mDerivedTypeAffinity;
    private String mFinalTypeAffinity;
    private String mObjectElementType;
    private boolean mNotNull;
    private int mPrimaryKeyPosition;
    private String mDefaultValue;
    private boolean mUnique;
    private boolean mRowidAlias;
    private boolean mAutoIncrementCoded;
    private String mColumnCreateSQL;

    /**
     * Full Constructor
     * @param columnName                The name of the column (as per sqlite_master)
     * @param alternativeColumnName     The enclosed name of the column (if found in the SQL)
     * @param owningTable               The name of the table to which the column belongs
     * @param columnType                The column type as coded
     * @param derivedType               The the column type as per SQLite Rules
     * @param finalType                 The final type as per ROOM types (may be guessed if NUMERIC)
     * @param objectElementType         The java type that would appear in the ROOM Entity
     * @param notNull                   flag indicating if not null constraint
     * @param cid                       The cid as per sqlite_master (no real use)
     * @param primaryKeyPosition        The position of the column in the primary key (0 if not)
     * @param defaultValue              Indicator if a default value is set (supplied if so)
     * @param unique                    flag indicating if the column has a UNIQUE constraint
     * @param rowidAlias                flag indicating id the row is an alias of the rowid column
     * @param autoIncCoded              flag indicating if AUTOINCREMENT is coded
     * @param SQL                       The SQL part that defines the column
     * @param originalColumnName        The original column name (see conditional processing)
     * @param originalAlternativeColumnName     The original alternative column name
     */
    public ColumnInfo(String columnName, String alternativeColumnName, String owningTable,
                      String columnType, String derivedType, String finalType, String objectElementType,
                      boolean notNull, int cid, int primaryKeyPosition, String defaultValue, boolean unique, boolean rowidAlias, boolean autoIncCoded, String SQL,
                      String originalColumnName, String originalAlternativeColumnName) {
        this.mColumnName = columnName;
        if (alternativeColumnName != null) {
            this.mAlternativeColumnName = alternativeColumnName;
        } else {
            this.mAlternativeColumnName = "";
        }

        // If the original name has been supplied (is greater then 0 characters in length)
        // then set the original name to the supplied name
        // else set the original name to be the column name
        if (originalColumnName.length() > 0) {
            this.mOriginalColumnName = originalColumnName;
        } else {
            this.mOriginalColumnName = columnName;
        }
        // Likewise for the alternative column name (enclosed as per SQL)
        if (originalAlternativeColumnName.length() > 0) {
            this.mOriginalEnclosedColumnName = originalAlternativeColumnName;
        } else {
            this.mOriginalEnclosedColumnName = alternativeColumnName;
        }
        this.mOwningTable = owningTable;
        this.mColumnType = columnType;
        this.mCID = cid;

        //If the dervied type has not been provided then generate it as per SQLite rules
        if (derivedType.length() < 1) {
            derivedType = convertTypeToDerivedAffinity(columnType);
        }
        this.mDerivedTypeAffinity = derivedType;

        //If the finalType has not been provied then generate it as per ROOM rules
        // i.e. must be INTEGER, TEXT, REAL or BLOB NOT NUMERIC
        if (finalType.length() < 1) {
            finalType = convertTypeToFinalAffinity(derivedType);
        }
        this.mFinalTypeAffinity = finalType;

        //Guess the java type to be used if not provided
        //  INTEGER = long, TEXT = String, REAL = double, BLOB = byte[]
        if (objectElementType.length() < 1) {
            this.mObjectElementType = guessObjectElementType(finalType);
        } else
            this.mObjectElementType = objectElementType;
        this.mNotNull = notNull;
        this.mPrimaryKeyPosition = primaryKeyPosition;
        if (defaultValue == null) {
            defaultValue = "";
        }
        this.mDefaultValue = defaultValue;
        this.mUnique = unique;
        this.mColumnCreateSQL = SQL;
        this.mRowidAlias = rowidAlias;
        this.mAutoIncrementCoded = autoIncCoded;
    }

    /**
     * Constructor for setting the original column/alt column as per the provided column names
     */
    public ColumnInfo(String columnName, String alternativeColumnName,
                      String owningTable,
                      String columnType, String derivedType, String finalType, String objectElementType,
                      boolean notNull, int cid, int primaryKeyPosition, String defaultValue, boolean unique, boolean rowidAlias, boolean autoIncCoded,
                      String SQL ) {

        this(
                columnName,alternativeColumnName,
                owningTable,
                columnType, derivedType,finalType,objectElementType,
                notNull,cid,primaryKeyPosition,defaultValue,unique,rowidAlias,autoIncCoded,
                SQL,
                "",""
        );
    }

    /**
     * Constructor for constructing based only upon PRAGMA column_info
     * @param columnName
     * @param owningTable
     * @param columnType
     * @param notnull
     * @param cid
     * @param primaryKeyPosition
     * @param defaultValue
     */
    public ColumnInfo(String columnName, String owningTable,
                      String columnType,
                      boolean notnull,int cid, int primaryKeyPosition, String defaultValue) {
        this(columnName,"",owningTable,
                columnType,"","","",
                notnull,cid,primaryKeyPosition,defaultValue,
                false, false, false, "");
    }

    /**
     * Empty Constructor
     */
    public ColumnInfo(){
        this("","","",
                "","","","",
                false,0,0,"",false,false,false,
                "","","");
    }

    /**
     * Convert column type to it's derived affinity
     * @param type  The column type coded in the declaration of the column
     * @return      The type affinity applied
     */
    private String convertTypeToDerivedAffinity(String type) {

        type = type.toUpperCase();
        if (type.contains(SQLiteConstants.AFFINITY_INTEGERRULE1_CONTAINS)) return AFFINITY_INTEGER;

        if (
                type.contains(SQLiteConstants.AFFINITY_TEXTRULE1_CONTAINS)
                        || type.contains(SQLiteConstants.AFFINITY_TEXTRULE2_CONTAINS)
                        || type.contains(SQLiteConstants.AFFINITY_TEXTRULE3_CONTAINS)
        ) return AFFINITY_TEXT;

        if (type.contains(SQLiteConstants.AFFINITY_BLOBRULE1_CONTAINS)) return AFFINITY_BLOB;

        if (
                type.contains(SQLiteConstants.AFFINITY_REALRULE1_CONTAINS)
                        || type.contains(SQLiteConstants.AFFINITY_REALRULE2_CONTAINS)
                        || type.contains(SQLiteConstants.AFFINITY_REALRULE3_CONTAINS)
        ) return AFFINITY_REAL;

        return AFFINITY_NUMERIC;
    }

    /**
     * Guess the java type based upon the final type affinity
     * @param finalType     The final SQlite type affinity
     * @return              The proposed java type
     */
    private String guessObjectElementType(String finalType) {
        if (finalType.equals(AFFINITY_BLOB)) {
            return  "byte[]";
        }
        if (finalType.equals(AFFINITY_REAL)) {
            return "Double";
        }
        if (finalType.equals(AFFINITY_TEXT)) {
            return "String";
        }
        if (finalType.equals(AFFINITY_INTEGER)) {
            return "Long";
        }
        else return ""; //????
    }

    /**
     * Convert the derived type affinity to the final type affinity
     * - in short if NUMERIC then change it to TEXT
     * @param derived
     * @return
     */
    private String convertTypeToFinalAffinity(String derived) {
        derived = derived.toUpperCase();
        if (derived.equals(AFFINITY_TEXT) ||
                derived.equals(AFFINITY_REAL) ||
                derived.equals(AFFINITY_INTEGER) ||
                derived.equals(AFFINITY_BLOB)) {
            return derived;
        }
        return AFFINITY_TEXT;
    }

    /**
     * Getters and Setters
     * @return
     */
    public String getColumnName() {
        return mColumnName;
    }

    public void setColumnName(String mColumnName) {
        this.mColumnName = mColumnName;
    }

    public String getOriginalColumnName() {
        return mOriginalColumnName;
    }

    public String getAlternativeColumnName() {
        return mAlternativeColumnName;
    }

    public void setAlternativeColumnName(String alternativeColumnName) {
        this.mAlternativeColumnName = alternativeColumnName;
    }

    public String getOriginalAlternativeColumnName() {
        return mOriginalEnclosedColumnName;
    }

    public String getOwningTable() {
        return mOwningTable;
    }

    public void setOwningTable(String owningTable) {
        this.mOwningTable = owningTable;
    }

    public String getColumnType() {
        return mColumnType;
    }

    public void setColumnType(String columnType) {
        this.mColumnType = columnType;
        this.mDerivedTypeAffinity = convertTypeToDerivedAffinity(columnType);
    }

    public String getDerivedTypeAffinity() {
        return mDerivedTypeAffinity;
    }

    public String getFinalTypeAffinity() {
        return mFinalTypeAffinity;
    }

    public String getObjectElementType() {
        return mObjectElementType;
    }

    public void setObjectElementType(String objectElementType) {
        this.mObjectElementType = objectElementType;
    }

    public void setFinalTypeAffinity(String finalTypeAffinity) {
        this.mFinalTypeAffinity = finalTypeAffinity;
    }

    public int getPrimaryKeyPosition() {
        return mPrimaryKeyPosition;
    }

    public void setPrimaryKeyPosition(int primaryKeyPosition) {
        this.mPrimaryKeyPosition = primaryKeyPosition;
    }

    public String getDefaultValue() {
        return mDefaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.mDefaultValue = defaultValue;
    }

    public boolean isNotNull() {
        return mNotNull;
    }

    public int getCID() {
        return mCID;
    }

    public void setNotNull(boolean notNull) {
        this.mNotNull = notNull;
    }

    public boolean isUnique() {
        return mUnique;
    }

    public void setUnique(boolean unique) {
        this.mUnique = unique;
    }

    public boolean isRowidAlias() {
        return mRowidAlias;
    }

    public void setRowidAlias(boolean rowidAlias) {
        this.mRowidAlias = rowidAlias;
    }

    public boolean isAutoIncrementCoded() {
        return mAutoIncrementCoded;
    }

    public void setAutoIncrementCoded(boolean autoIncrementCoded) {
        this.mAutoIncrementCoded = autoIncrementCoded;
    }

    public String getColumnCreateSQL() {
        return mColumnCreateSQL;
    }

    public void setColumnCreateSQL(String columnCreateSQL) {
        this.mColumnCreateSQL = columnCreateSQL;
    }

    @NonNull
    @Override
    public String toString() {
        return mColumnName + " coded Type " + mColumnType + " derived Type " + mDerivedTypeAffinity;
    }

}