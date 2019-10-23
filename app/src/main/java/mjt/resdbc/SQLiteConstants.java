package mjt.resdbc;

public class SQLiteConstants {

    public static final String SQLITEFILEHEADER = "SQLite format 3\u0000";
    public static final String SQLITESYSTEMTABLEPREFIX = "sqlite_";

    public static final String SQLITEMASTER_TABLENAME = "sqlite_master";
    public static final String SQLITEMASTER_COL_NAME = "name";
    public static final String SQLITEMASTER_COL_TYPE = "type";
    public static final String SQLITEMASTER_COL_TBLNAME = "tbl_name";
    public static final String SQLITEMASTER_COL_ROOTPAGE = "rootpage";
    public static final String SQLITEMASTER_COL_SQL = "sql";
    public static final String SQLITEMASTERTYPE_TABLE = "table";
    public static final String SQLITEMASTERTYPE_INDEX = "index";
    public static final String SQLITEMASTERTYPE_TRIGGER = "trigger";
    public static final String SQLITEMASTERTYPE_VIEW = "view";

    public static final String SQLITETABLEINFO_TABLENAME = "table_info";
    public static final String SQLITETABLEINFO_COL_CID = "cid";
    public static final String SQLITETABLEINFO_COL_NAME = "name";
    public static final String SQLITETABLEINFO_COL_TYPE = "type";
    public static final String SQLITETABLEINFO_COL_NOTNULL = "notnull";
    public static final String SQLITETABLEINFO_COL_DFLTVALUE = "dflt_value";
    public static final String SQLITETABLEINFO_COL_PRIMARYKEY = "pk";

    public static final String SQLITEINDEXINFO_TABLENAME = "index_info";
    public static final String SQLITEINDEXINFO_COl_SEQNO = "seqno";
    public static final String SQLITEINDEXINFO_COL_INDEXRANK = SQLITEINDEXINFO_COl_SEQNO;
    public static final String SQLITEINDEXINFO_COL_CID = "cid";
    public static final String SQLITEINDEXINFO_COL_TABLERANK = SQLITEINDEXINFO_COL_CID;
    public static final String SQLITEINDEXINFO_COl_NAME = "name";


    public static final String AFFINITY_INTEGER = "INTEGER";
    public static final String AFFINITY_TEXT = "TEXT";
    public static final String AFFINITY_BLOB = "BLOB";
    public static final String AFFINITY_REAL = "REAL";
    public static final String AFFINITY_NUMERIC = "NUMERIC";
    public static final String[] ROOM_AFFINITIES = new String[]{
            AFFINITY_INTEGER,
            AFFINITY_TEXT,
            AFFINITY_REAL,
            AFFINITY_BLOB
    };

    public static final String AFFINITY_INTEGERRULE1_CONTAINS = "INT"; // rule 1
    public static final String AFFINITY_TEXTRULE1_CONTAINS = "CHAR"; // rule 2
    public static final String AFFINITY_TEXTRULE2_CONTAINS = "CLOB"; // rule 3
    public static final String AFFINITY_TEXTRULE3_CONTAINS = "TEXT"; // rule 4
    public static final String AFFINITY_BLOBRULE1_CONTAINS = "BLOB"; // rule 5
    public static final String AFFINITY_REALRULE1_CONTAINS = "REAL"; // rule 6
    public static final String AFFINITY_REALRULE2_CONTAINS = "FLOA"; // rule 7
    public static final String AFFINITY_REALRULE3_CONTAINS = "DOUB"; // rule 8

    public static final String SQLKEYWORD_WITHOUTROWID = "WITHOUT ROWID";

    public static final  String ANDROID_METADATA_TABLE = "android_metadata";
    public static final String ROOM_MASTER_TABLE = "room_master_table";

    public static final String KEYWORD_CREATE = "CREATE";
    public static final String KEYWORD_TABLE = "TABLE";
    public static final String KEYWORD_VIRTUAL = "VIRTUAL";
    public static final String CLAUSE_CREATEVIRTTBL = KEYWORD_CREATE + " " +  KEYWORD_VIRTUAL + " " + KEYWORD_TABLE;
    public static final String KEYWORD_USING = "USING";
    public static final String KEYWORD_INDEX = "INDEX";
    public static final String KEYWORD_WHERE = "WHERE";
    public static final String KEYWORD_UNIQUE = "UNIQUE";
    public static final String KEYWORD_DEFAULT = "DEFAULT";
    public static final String KEYWORD_DEFERRABLE = "DEFERRABLE";
    public static final String KEYWORD_INITIALLY = "INITIALLY";
    public static final String KEYWORD_DEFERRED = "DEFERRED";
    public static final String CLAUSE_IFNOTEXISTS = "IF NOT EXISTS";
    public static final String KEYWORD_NOTNULL = "NOT NULL";
    public static final String CLAUSE_PRIMARYKEY = "PRIMARY KEY";
    public static final String CLAUSE_AUTOINCREMENT = CLAUSE_PRIMARYKEY + " AUTOINCREMENT " + KEYWORD_NOTNULL;
    public static final String GROUP_START = "(";
    public static final String GROUP_END = ")";

    public static final String KEYWORD_ON = "ON";

    public static final String CLAUSE_FOREIGNKEY_START = "FOREIGN KEY";
    public static final String KEYWORD_REFERENCES = "REFERENCES";
    public static final String CLAUSE_FOREIGNKEY_ONUPDATE =  KEYWORD_ON +  " UPDATE";
    public static final String CLAUSE_FOREIGNKEY_ONDELETE =  KEYWORD_ON + " DELETE";
    public static final String CLAUSE_FOREIGNKEY_DEFERRABLE = "DEFERRABLE INITIALLY DEFERRED";


    public static final String ROOMTYPE_BOOLEANPRIMARY = "boolean";
    public static final String ROOMTYPE_BOOLEANOBJECT = "Boolean";
    public static final String ROOMTYPE_BYTEPRIMARY = "byte";
    public static final String ROOMTYPE_BYTEOBJECT = "Byte";
    public static final String ROOMTYPE_CHARPRIMARY = "char";
    public static final String ROOMTYPE_CHAROBJECT = "Character";
    public static final String ROOMTYPE_SHORTPRIMARY = "short";
    public static final String ROOMTYPE_SHORTOBJECT = "Short";
    public static final String ROOMTYPE_INTPRIMARY = "int";
    public static final String ROOMTYPE_INTOBJECT = "Integer";
    public static final String ROOMTYPE_LONGPRIMARY = "long";
    public static final String ROOMTYPE_LONGOBJECT = "Long";
    public static final String ROOMTYPE_BIGINT = "BigInteger";

    public static final String[] ROOMTYPES_INTEGER = new String[]{
            ROOMTYPE_BOOLEANPRIMARY,
            ROOMTYPE_BOOLEANOBJECT,
            ROOMTYPE_BYTEPRIMARY,
            ROOMTYPE_BYTEOBJECT,
            ROOMTYPE_CHARPRIMARY,
            ROOMTYPE_CHAROBJECT,
            ROOMTYPE_SHORTPRIMARY,
            ROOMTYPE_SHORTOBJECT,
            ROOMTYPE_INTPRIMARY,
            ROOMTYPE_INTOBJECT,
            ROOMTYPE_LONGPRIMARY,
            ROOMTYPE_LONGOBJECT,
            ROOMTYPE_BIGINT
    };
    public static final String ROOMTYPE_STRING = "String";
    public static final String[] ROOMTYPES_TEXT = new String[]{
            ROOMTYPE_STRING
    };
    public static final String ROOMTYPE_FLOATPRIMARY = "float";
    public static final String ROOMTYPE_FLOATOBJECT = "Float";
    public static final String ROOMTYPE_DOUBLEPRIMARY = "double";
    public static final String ROOMTYPE_DOUBLEOBJECT = "Double";
    public static final String ROOMTYPE_BIGDECIMAL = "BigDecimal";
    public static final String[] ROOMTYPES_REAL = new String[]{
            ROOMTYPE_FLOATPRIMARY,
            ROOMTYPE_FLOATOBJECT,
            ROOMTYPE_DOUBLEPRIMARY,
            ROOMTYPE_DOUBLEOBJECT,
            ROOMTYPE_BIGDECIMAL
    };
    public static final String ROOMTYPE_BYTERARRAYPRIMARY = "byte[]";
    public static final String ROOMTYPE_BYTEARRAYOBJECT = "Byte[]";
    public static final String[] ROOMTYPES_BLOB = new String[]{
            ROOMTYPE_BYTERARRAYPRIMARY,
            ROOMTYPE_BYTEARRAYOBJECT
    };

    public static final String[] SUPPORTEDVIRTUALTABLEMODULES = new String[]{
            "FTS3","FTS4"
    };
}