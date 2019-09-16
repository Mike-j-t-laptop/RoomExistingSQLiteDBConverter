# RoomExistingSQLiteDBConverter #

## What the App does. ##
App to convert an Exisiting non-Room database for Room, generating **@Entity**, **@Dao** and **@Database** code as well as converting the database.

- *Andorid's ROOM expects the SQLite Database schema to adhere to it's rules. It is the **@Entity**'s that define the expected schema.*
- *As such converting the database is relatively simple in comparison to generating the correct **@Entity**'s, especially if the underlying rules are unknown; and hence why the App can be useful.* 
- *Android's ROOM does now provide a conversion utility, but it only converts the database, the task of creating the **exact*** ***@Entity**'s, is still required.*

That is the App produces 4 distinct sets of data :-

1. The Converted database.
2. The @Entity's, one per table in the underlying database.
3. @Dao's for the most basic operations (insert/update/delete and retrieve all), again oe per table.

  -  ***Note**, the generated code lacks the import statements (this is intentional, as they can vary). As such every generated file should be visited to resolve the imports (and if no package has been supplied the package statement as well).*
4. A single @Database file that includes the entities definition (i.e. includes the @Entity classes) and abstact methods for the invocation of the @Dao's.

## What the App does not do. ##

1. The App only supports limited **VIRTUAL TABLE** support (just FTS3 and FTS4).
1. **WITHOUT ROWID** tables will be converted to **ROWID** Tables (untested).
2. Currently there is no protection against invalid class/dao file names
 1. e.g. if a table were named **1table** and it was enclosed, as it would have to be, e.g. **`1table`** then the resultant @Entity file would be **1table.java** and the @Dao file would be **1tableDao** (untested as to exaclty what the connotations are)

## An Overview of how the App works ##


### 1. Retrieves the databases

1. The App initially searches the External Publc Storage (often called sdcard (but typically not on an sdcard)) for SQLite databases (it looks for/at the SQLite header), 
except any in the reserved directory **RoomDBConverterDBConversions** and lists the databases.
Note encrypted databases are not handled.

- **Note**, when first run, permission will be requested. If not provided no Database will be listed.
- The **REFRESH** button will refresh the list (e.g. after adding a database).
- The **RoomDBConverterDBConversions** directory is where the data from the conversion (the databases and the java code) is placed.

### 2. Inspects the selected database
2. Selecting a Database file, display information about the Database, and initially the Tables.
Clicking Tables, Columns, Indexes, FK, Triggers or Views, switches to display information about the respetive componenets.

1. Warnings/changes are highlighted in the component. 
  1. Orange is more informational.
(e.g. type affinity VARCHAR(100) will be changed to TEXT (it's derived type affinity as per SQLite)).
  1. Red indicates a more prominent change or issue.
(e.g. a type affinity of DATETIME is changed to TEXT (Final) as the derived affinity is NUMERIC which is not supported by Room).

### 3. Tailors the Conversion output

The **Convert** section allows :-

- Specifying the sub-directory of the **RoomDBConverterDBConversions** directory *(this cannot be changed as it needs to be specififc so that converted databases aren't located when retrieving the databases)*.
  - All data from a conversion will be placed into a sub-directory of the **RoomDBConverterDBConversions**. By default the sub-directory used will be **Convert_** suffixed with the database file name.
- A package name.
  - If supplied, this can reduce the work involved of adding the package statements to the @Entity and @Dao code for each table.
  - If not suppled then package statements will not be added to the resultant code, they will then have to be added to each file *i.e. the number of tables * 2 (1 per @Entity, 1 per @Dao)*.
  - Obviously the package name has to be correct.
  - Determining and supplying the package name is recommended, as it can be possible to inadvertently utililse the wrong classes if using ALT + ENTER to resolve imports (import statements are not included in the generated code).

- Specifying the mode (whether or not Database component names are enclosed)
  - **Note**, the coverter uses the name as is stored in sqlite_master and thus the name will not be enclosed. Using SafeMode encloses the component names in **`**,s (grave accents). So invalid table names are valid. Unchecking SafeMode may result in tables not being able to be created.
  - *Note, the intention is to add a third ultra-safe mode in which case component names will be pre-prefixed (e.g. with something like rc_).*

- The specification of the sub-sub-directory for the @Entity and additionaly the @Dao files.
  - By default **java** is used for both so @Entity and @Dao files are store together (this allows for a single copy say from device-explorer).
  - The **@Database** file will be placed into the same directory as the **@Entity** files.
    - The **@Database** file is the capitalised Database Name, less the file extension, suffixed with Database and .java; e.g. if the database name is RoomAssetConversion.db then the **@Database** file will **RoomAssetConversionDatabase.java**

- Conversion
  - Clicking the **CONVERT**- button will attempt to undertake the conversion.
  - A dialog appears with messages. 


# Example Usage (user guide) #

## Initial Display ##
When the App is first started **(after accepting permission)** then the database files found within the External Public Storage are displayed (if any).
- If permisssion is not granted then no files/database will be found as read access to the directory prevents any being found.
- If there are none then the database(s) to be converted should be copied into External Public Storage (typically this is sdcard aka storage/emulated/0, however it can be device dependant *(see [https://developer.android.com/training/data-storage/files](https://developer.android.com/training/data-storage/files))*

![Example 001](https://i.imgur.com/yBYcBdl.png)

Here you can see that 7 databases have been located in various directories.

## Selecting and Inspecting a Database ##

If a listed database is selected (clicked) then the database will be inspected. 
The following is the result of clicking the last (the database file **RoomAssetConversion.db**, which is located in the RoomDBConverterDBSource folder) :-

![Example 002](https://i.imgur.com/ohe6W3x.png)

As can be seen from the above three additional sections are added:-

1. The Convert section (light red).
 1. The CONVERT button initiates the Conversion.
2. The Database Information section (green).
3. The Component section (yellow).
 1. The component section initially shows the tables.
 2. Clicking on Tables, Columns, Indexes, FK, Triggers or Views switches to displaying respective information.

### Tables View ###

Lists information about each table. If a table is a deviation from a usual table and has to be treated differently then the table name will be followed by relevant information.

#### Deviations/notifications are :- ####

1. **Indicates a room master table, this will be omitted from the converted database.** (RED).
 1. ROOM manages the room_master table, it may lead to issues if this table were converted/copied. It is also likely that there is no neeed to convert such a database.
1. **Room requires all entities (tables) to have a primary key. Will generate primary key from all columns.** (RED).
2. **Indicates a virtual table, this will be omitted from the converted database.** (RED).
 1. Limited support is provided for VIRTUAL tables, that is only for FTS3 and FTS4. If FTS3 or FTS4.
 2. 
3. **USING supported module ????** (Orange) (where ???? is the module name, as above). 
 1. The VIRTUAL table will be converted. However, the other FTS tables, which are generated by the FTS module will be omitted as per the deviation/notification :-
4. **Indicates a FTS table, this will be generated, there will be no @Entity or @Dao files created for this table.** (RED)

![KJBibleFTSNotifications](https://i.imgur.com/zenv7aE.png)

### Columns View  ###

Lists information about each column in the database. Notifications can appear with the respective attribute's value.

#### Notifications are :- ####
1. **Indicates a room master table, this will be omitted from the converted database.** (RED - with the Table attribute's value).
1. **The specified type affinity is not usable in Room, the derived type will be used (if not NUMERIC).** (Orange -  with the Derived attribute's value).
 1. If the Type Affinity used when defining the original/source table is not one of the four supported by ROOM (TEXT, INTEGER, REAL or BLOB), then this notifications indicates that it will be changed.
1. **NUMERIC type detected. Using TEXT and therefore object member will be String.** (RED - with the FINAL attribute's value).
 1. Using **String** as the object type is the most easily converted option e.g. it could cater if DATETIME were the original type affinity with timestamps or date/timestrings.
1. **AUTOINCREMENT will be added.** (RED - with the Autoincrement Coded attribute's value).
 1. For automatically generated rowid's the **@Entity** includes **`@PrimaryKey(autoGenerate = true)`** and this requires that **`AUTOINCREMENT`** is coded (as well as `NOT NULL`).
1. **A default value has been detected. You may wish to set the object member to default to the value.** (RED - with the Default attribute's value). 



### Indexes, FK, Triggers and Views ###

These views simply provide the information.

1. Indexes will be created.
2. Foreign Keys will be coded at the TABLE LEVEL.
3. Triggers will be created using the SQL extracted from sqlite_master.
4. Views are currently ignored.

## Convert ##

Clicking the **CONVERT** button result in a dialog displaying the results of the conversion. 

For example when converting the **RoomAssetConversion.db** ([the Chinook Database](http://https://www.sqlitetutorial.net/sqlite-sample-database/)), the result is :-

![Example 003](https://i.imgur.com/8eTr1jC.png)

That is bar the WARNING the conversion was successfull.

- The WARNING is due to 1 row not being copied in the **employees** table. 
- This is because the database has an internal foreign key on the **reportsTo** column and that NULL is used to indicate reporting to nobody. This in conjunction with ROOM requiring that the indexes for Foreign Keys must have NOT NULL.

If there are issues then the dialog should display such issues.


# Testing #

Limited testing has been undertaken. Initally 3 Databases were used :-

- The previously mentioned [Chinook Database](http://https://www.sqlitetutorial.net/sqlite-sample-database/)
 - 11 Tables, with 64 Columns, 10 Indxexes, 11 Foreign keys. 
- A version of the King James Bible (include FTS (Full Text Search)).
 - 2 Tables and an FTS table that generates 3 other tables. 
- A Shopping List database.
 - 8 Tables, 54 Columns, 3 Indxexes.


For each an initial App was created with the main activity including methods to copy the database from the assets folder which is invoked prior to the build of the ROOM database, and also methods using the copied database, invoked after the room build, to access the data as a prrof of conversion. 

For example, MainActivity.java for the Shopwise database was :-

	public class MainActivity extends AppCompatActivity {
	    
	    ShopwiseDatabase mRoomDB;
	    
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    /**
	     * The following code can be used to copy the database from the assets file before Room
	     * as an alternative to using the Room createFromAsset.
	     * Note that if used then the Room createFromAsset will do nothing
	     */
	    //Start of code for copying database from the assets
	    copyDBFromAssets(this,ShopwiseDatabase.ASSETFILENAME,ShopwiseDatabase.DBNAME);
	    //Dump the table create SQL to the log for
	    SQLiteDatabase db = SQLiteDatabase.openDatabase(this.getDatabasePath(ShopwiseDatabase.DBNAME).getPath(),null,SQLiteDatabase.OPEN_READWRITE);
	    Cursor csr = db.query("sqlite_master",new String[]{"sql"},"type='table'",null,null,null,null);
	    
	    while (csr.moveToNext()) {
	    Log.d("TABLESQL",csr.getString(0));
	    }
	    csr.close();
	    db.close();
	    //End of code for copying and dumping the pre-room copied database
	    
	    // Build the Room database (note for testing/brevity run on main thread)
	    mRoomDB = Room.databaseBuilder(this,ShopwiseDatabase.class,ShopwiseDatabase.DBNAME)
	    //.createFromAsset(ChinookDatabase.DBNAME) //<<<<<<<<<< uses Room's Convert (will do nothing if asset already copied)
	    .allowMainThreadQueries()
	    .build();
	    
        //Use each Table's getEvery????? method to extract all rows from the table (?????)
	    dumpObject((List)mRoomDB.getAislesDao().getEveryAisles());
	    dumpObject((List)mRoomDB.getAppValuesDao().getEveryAppvalues());
	    dumpObject((List)mRoomDB.getProductsDao().getEveryProducts());
	    dumpObject((List)mRoomDB.getProductUsageDao().getEveryProductusage());
	    dumpObject((List)mRoomDB.getRulesDao().getEveryRules());
	    dumpObject((List)mRoomDB.getShopListDao().getEveryShoplist());
	    dumpObject((List)mRoomDB.getShopsDao().getEveryShops());
	    dumpObject((List)mRoomDB.getStorageDao().getEveryStorage());
	    }
	    
	    /**
	     * Dump a few extracted objects using the toString method (typically the default)
	     * @param objectListThe list of objects
	     */
	    private void dumpObject(List<Object> objectList) {
	    int maxToDisplay = 5;
	    int displayed = 0;
	    Log.d("OBJECTDUMP","Dumping a maximum of " + String.valueOf(maxToDisplay) + " objects from " + String.valueOf(objectList.size()) +".");
	    
	    for (Object o: objectList) {
	    if (displayed >= maxToDisplay) break;
	    if (displayed == 0) {
	    
	    }
	    Log.d("OBJECTDUMP","Object as from class " + o.getClass().getName().toString() + " String represntation is" + o.toString());
	    displayed++;
	    }
	    }
	    
	    /**
	     * Copy the database from the assets folder, as an alternative to the ROOM createFromAsset
	     * @param context   The context (for determining the database location and retrieving the asset)
	     * @param assetFileName The path of the asset file within the assets folder
	     * @param databaseName  The name of the database (typically the same but can be different if not using createFromAsset)
	     */
	    private void copyDBFromAssets(Context context, String assetFileName, String databaseName) {
	    if (checkIfDBExists(context,databaseName)) return;
	    File databaseFile = new File(context.getDatabasePath(databaseName).toString());
	    try {
	    InputStream is = context.getAssets().open(assetFileName);
	    OutputStream os = new FileOutputStream(databaseFile);
	    byte[] buffer = new byte[1024 * 32];
	    int length;
	    while ((length = is.read(buffer)) > 0) {
	    os.write(buffer,0,length);
	    }
	    os.flush();
	    os.close();
	    is.close();
	    
	    } catch (IOException e) {
	    e.printStackTrace();
	    throw new RuntimeException("Error copying asset database");
	    }
	    }
	    
	    /**
	     * Check if the database exists and if not that any intermediate directories exist
	     * @param context   The context (for determining the database path)
	     * @param databaseName  The database name
	     * @return  True if the database already exists, false if not
	     */
	    private boolean checkIfDBExists(Context context, String databaseName) {
	    File dbfile = new File(context.getDatabasePath(databaseName).toString());
	    if (dbfile.exists()) return true;
	    if (!dbfile.getParentFile().exists()) {
	    dbfile.getParentFile().mkdirs();
	    }
	    return false;
	    }
	}

In addition to the above a second file, the @Database file was manually created. For the ShopWise database the file **ShopwiseDatabase.java** was :-

	@Database(version = 1, entities = {
	        Aisles.class,
	        Appvalues.class,
	        Products.class,
	        Productusage.class,
	        Rules.class,
	        Shoplist.class,
	        Shops.class,
	        Storage.class
	})
	
	public abstract class ShopwiseDatabase extends RoomDatabase {
	
	    public static final String DBNAME = "Shopwise";
	    public static final String ASSETFILENAME = "ShopWiseDB_201904202247.bkp";
	
	    public abstract AislesDao getAislesDao();
	    public abstract AppvaluesDao getAppValuesDao();
	    public abstract ProductsDao getProductsDao();
	    public abstract ProductusageDao getProductUsageDao();
	    public abstract RulesDao getRulesDao();
	    public abstract ShoplistDao getShopListDao();
	    public abstract ShopsDao getShopsDao();
	    public abstract StorageDao getStorageDao();
	}

### Note a @Database file is now generated ###

Since the addition of the functionality to generate the @Database file, the following file would be generated in the same directory as the @Entity files :-

    package your_package_name;
    
    @Database(version=1,entities = {
    	Shops.class,
    	Aisles.class,
    	Products.class,
    	Productusage.class,
    	Shoplist.class,
    	Rules.class,
    	Appvalues.class,
    	Storage.class
    })
    public abstract class ShopWiseDB_201904202247Database extends RoomDatabase {
    
    	public static final String DBNAME = "ShopWiseDB_201904202247.bkp";
    
    	public abstract ShopsDao getShopsDao();
    	public abstract AislesDao getAislesDao();
    	public abstract ProductsDao getProductsDao();
    	public abstract ProductusageDao getProductusageDao();
    	public abstract ShoplistDao getShoplistDao();
    	public abstract RulesDao getRulesDao();
    	public abstract AppvaluesDao getAppvaluesDao();
    	public abstract StorageDao getStorageDao();
    }

 - **Note** that this file would need to be tailored to suite the code above so that DBNAME has a value of **Shopwise** and that the constant ASSETFILENAME is introduced with a value of **ShopWiseDB_201904202247.bkp**

All the other 16 files (an @Entity file and an @Dao file per table) were generated by the App, copied into the App using Android Studio's **Device Explorer**, and then modified to generate the **import** statements.

Screen shot from the Device Explorer Window :-

![Example 004](https://i.imgur.com/rkEaaQW.png)

1. Is the reserved conversion Directory into which all conversions are placed.
2. Is the sub-directory for the selected Shopwise database (if one of the others were selected, as the database name differs then it would have it's own directory).
3. Is the converted database file that is copied to the assets folder (can be copied from Device Explorer, so the conversion can be run on an emulator *(all testing was done using an Android Studio emulator and using Device Explorer)*, a real device could be used in which case the files could be copied via file transfer or Device Explorer if the device is available to Android Studio).
4. In this case all files (@Entity, @Dao and @Database (note the @Database file is not shown in the above screenshot)) are highlighted by the blue closing brace. These files would a) be copied (perhaps using Device Explorer (select all, right click and Save As)) and then b) editted to resolve imports.


In addition to writing the code the database file is copied to the App's assets folder.

The App when run produces the following output (not that SafeMode was used and hence component names are enclosed in ``) :-

	2019-08-30 12:10:02.843 32047-32047/exacnv.exampleconvertedshopwisedb D/TABLESQL: CREATE TABLE android_metadata (locale TEXT)
	2019-08-30 12:10:02.843 32047-32047/exacnv.exampleconvertedshopwisedb D/TABLESQL: CREATE TABLE `shops`(`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,`shoporder` INTEGER,`shopname` TEXT,`shopstreet` TEXT,`shopcity` TEXT,`shopstate` TEXT,`shopnotes` TEXT)
	2019-08-30 12:10:02.843 32047-32047/exacnv.exampleconvertedshopwisedb D/TABLESQL: CREATE TABLE sqlite_sequence(name,seq)
	2019-08-30 12:10:02.843 32047-32047/exacnv.exampleconvertedshopwisedb D/TABLESQL: CREATE TABLE `aisles`(`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,`aisleshopref` INTEGER,`aisleorder` INTEGER,`aislename` TEXT)
	2019-08-30 12:10:02.843 32047-32047/exacnv.exampleconvertedshopwisedb D/TABLESQL: CREATE TABLE `products`(`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,`productname` TEXT,`productnotes` TEXT,`productstorageref` INTEGER,`productstorageorder` INTEGER)
	2019-08-30 12:10:02.843 32047-32047/exacnv.exampleconvertedshopwisedb D/TABLESQL: CREATE TABLE `productusage`(`productusageproductref` INTEGER NOT NULL,`productusageaisleref` INTEGER NOT NULL,`productusagecost` REAL,`productusagebuycount` INTEGER,`productusagefirstbuydate` INTEGER,`productusagelatestbuydate` INTEGER,`productusageorder` INTEGER,`productusagerulesuggestflag` INTEGER,`productusagechecklistflag` INTEGER,`productusagechecklistcount` INTEGER,PRIMARY KEY(`productusageproductref`,`productusageaisleref`))
	2019-08-30 12:10:02.843 32047-32047/exacnv.exampleconvertedshopwisedb D/TABLESQL: CREATE TABLE `shoplist`(`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,`shoplistproductref` INTEGER,`shoplistaisleref` INTEGER,`shoplistdateadded` INTEGER,`shoplistnumbertoget` INTEGER,`shoplistdone` INTEGER,`shoplistdategot` INTEGER,`shoplistcost` REAL)
	2019-08-30 12:10:02.843 32047-32047/exacnv.exampleconvertedshopwisedb D/TABLESQL: CREATE TABLE `rules`(`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,`ruleproductref` INTEGER,`ruleaisleref` INTEGER,`rulename` TEXT,`ruleuses` INTEGER,`ruleprompt` INTEGER,`ruleacton` INTEGER,`ruleperiod` INTEGER,`rulemultiplier` INTEGER)
	2019-08-30 12:10:02.843 32047-32047/exacnv.exampleconvertedshopwisedb D/TABLESQL: CREATE TABLE `appvalues`(`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,`appvaluename` TEXT,`appvaluetype` TEXT,`appvalueint` INTEGER,`appvaluereal` REAL,`appvaluetext` TEXT,`appvalueincludeinsettings` INTEGER,`appvaluesettingsinfo` TEXT)
	2019-08-30 12:10:02.843 32047-32047/exacnv.exampleconvertedshopwisedb D/TABLESQL: CREATE TABLE `storage`(`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,`storageorder` INTEGER,`storagename` TEXT)

and :-

	2019-08-30 12:10:02.859 32047-32047/exacnv.exampleconvertedshopwisedb I/ertedshopwised:     at void com.android.internal.os.ZygoteInit.main(java.lang.String[]) (ZygoteInit.java:858)
	2019-08-30 12:10:02.948 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Dumping a maximum of 5 objects from 91.
	2019-08-30 12:10:02.948 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Aisles String represntation isexacnv.exampleconvertedshopwisedb.Aisles@32ad3a8
	2019-08-30 12:10:02.948 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Aisles String represntation isexacnv.exampleconvertedshopwisedb.Aisles@1a97ec1
	2019-08-30 12:10:02.948 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Aisles String represntation isexacnv.exampleconvertedshopwisedb.Aisles@40ff666
	2019-08-30 12:10:02.948 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Aisles String represntation isexacnv.exampleconvertedshopwisedb.Aisles@c32da7
	2019-08-30 12:10:02.948 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Aisles String represntation isexacnv.exampleconvertedshopwisedb.Aisles@12d8254
	2019-08-30 12:10:02.961 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Dumping a maximum of 5 objects from 107.
	2019-08-30 12:10:02.961 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Appvalues String represntation isexacnv.exampleconvertedshopwisedb.Appvalues@f0e45fd
	2019-08-30 12:10:02.961 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Appvalues String represntation isexacnv.exampleconvertedshopwisedb.Appvalues@11512f2
	2019-08-30 12:10:02.961 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Appvalues String represntation isexacnv.exampleconvertedshopwisedb.Appvalues@aeafd43
	2019-08-30 12:10:02.961 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Appvalues String represntation isexacnv.exampleconvertedshopwisedb.Appvalues@40a6fc0
	2019-08-30 12:10:02.962 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Appvalues String represntation isexacnv.exampleconvertedshopwisedb.Appvalues@4fe44f9
	2019-08-30 12:10:02.968 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Dumping a maximum of 5 objects from 151.
	2019-08-30 12:10:02.968 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Products String represntation isexacnv.exampleconvertedshopwisedb.Products@add6c3e
	2019-08-30 12:10:02.968 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Products String represntation isexacnv.exampleconvertedshopwisedb.Products@d362a9f
	2019-08-30 12:10:02.968 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Products String represntation isexacnv.exampleconvertedshopwisedb.Products@706c7ec
	2019-08-30 12:10:02.968 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Products String represntation isexacnv.exampleconvertedshopwisedb.Products@b5537b5
	2019-08-30 12:10:02.968 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Products String represntation isexacnv.exampleconvertedshopwisedb.Products@5400e4a
	2019-08-30 12:10:02.980 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Dumping a maximum of 5 objects from 152.
	2019-08-30 12:10:02.980 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Productusage String represntation isexacnv.exampleconvertedshopwisedb.Productusage@7dcd1bb
	2019-08-30 12:10:02.980 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Productusage String represntation isexacnv.exampleconvertedshopwisedb.Productusage@28e76d8
	2019-08-30 12:10:02.980 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Productusage String represntation isexacnv.exampleconvertedshopwisedb.Productusage@1ee9a31
	2019-08-30 12:10:02.980 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Productusage String represntation isexacnv.exampleconvertedshopwisedb.Productusage@878c516
	2019-08-30 12:10:02.980 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Productusage String represntation isexacnv.exampleconvertedshopwisedb.Productusage@c7cce97
	2019-08-30 12:10:02.984 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Dumping a maximum of 5 objects from 5.
	2019-08-30 12:10:02.984 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Rules String represntation isexacnv.exampleconvertedshopwisedb.Rules@7e02884
	2019-08-30 12:10:02.984 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Rules String represntation isexacnv.exampleconvertedshopwisedb.Rules@e1a86d
	2019-08-30 12:10:02.984 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Rules String represntation isexacnv.exampleconvertedshopwisedb.Rules@2b41ca2
	2019-08-30 12:10:02.984 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Rules String represntation isexacnv.exampleconvertedshopwisedb.Rules@eb5bd33
	2019-08-30 12:10:02.984 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Rules String represntation isexacnv.exampleconvertedshopwisedb.Rules@b7948f0
	2019-08-30 12:10:02.988 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Dumping a maximum of 5 objects from 4.
	2019-08-30 12:10:02.988 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Shoplist String represntation isexacnv.exampleconvertedshopwisedb.Shoplist@7d5e69
	2019-08-30 12:10:02.988 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Shoplist String represntation isexacnv.exampleconvertedshopwisedb.Shoplist@65b60ee
	2019-08-30 12:10:02.988 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Shoplist String represntation isexacnv.exampleconvertedshopwisedb.Shoplist@284f98f
	2019-08-30 12:10:02.989 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Shoplist String represntation isexacnv.exampleconvertedshopwisedb.Shoplist@a42041c
	2019-08-30 12:10:02.992 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Dumping a maximum of 5 objects from 7.
	2019-08-30 12:10:02.993 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Shops String represntation isexacnv.exampleconvertedshopwisedb.Shops@5047825
	2019-08-30 12:10:02.993 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Shops String represntation isexacnv.exampleconvertedshopwisedb.Shops@1209dfa
	2019-08-30 12:10:02.993 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Shops String represntation isexacnv.exampleconvertedshopwisedb.Shops@4619fab
	2019-08-30 12:10:02.993 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Shops String represntation isexacnv.exampleconvertedshopwisedb.Shops@5794608
	2019-08-30 12:10:02.993 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Shops String represntation isexacnv.exampleconvertedshopwisedb.Shops@a2971a1
	2019-08-30 12:10:02.995 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Dumping a maximum of 5 objects from 31.
	2019-08-30 12:10:02.995 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Storage String represntation isexacnv.exampleconvertedshopwisedb.Storage@7ca9fc6
	2019-08-30 12:10:02.995 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Storage String represntation isexacnv.exampleconvertedshopwisedb.Storage@7188b87
	2019-08-30 12:10:02.995 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Storage String represntation isexacnv.exampleconvertedshopwisedb.Storage@160bab4
	2019-08-30 12:10:02.995 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Storage String represntation isexacnv.exampleconvertedshopwisedb.Storage@a4a86dd
	2019-08-30 12:10:02.995 32047-32047/exacnv.exampleconvertedshopwisedb D/OBJECTDUMP: Object as from class exacnv.exampleconvertedshopwisedb.Storage String represntation isexacnv.exampleconvertedshopwisedb.Storage@2c0f252


For the King James Bible the output (fome similar code) was :-

	2019-08-30 12:16:01.259 32408-32408/exacnv.examplekjbible D/TABLESQL: CREATE TABLE android_metadata (locale TEXT)
	2019-08-30 12:16:01.259 32408-32408/exacnv.examplekjbible D/TABLESQL: CREATE TABLE `bible`(`book` TEXT NOT NULL,`chapter` INTEGER NOT NULL,`verse` INTEGER NOT NULL,`content` TEXT,PRIMARY KEY(`book`,`chapter`,`verse`))
	2019-08-30 12:16:01.259 32408-32408/exacnv.examplekjbible D/TABLESQL: CREATE TABLE `metadata`(`k` TEXT NOT NULL,`v` TEXT NOT NULL,PRIMARY KEY(`k`,`v`))
	2019-08-30 12:16:01.259 32408-32408/exacnv.examplekjbible D/TABLESQL: CREATE VIRTUAL TABLE bible_fts USING FTS3(book, chapter INTEGER, verse INTEGER, content TEXT)
	2019-08-30 12:16:01.259 32408-32408/exacnv.examplekjbible D/TABLESQL: CREATE TABLE 'bible_fts_content'(docid INTEGER PRIMARY KEY, 'c0book', 'c1chapter', 'c2verse', 'c3content')
	2019-08-30 12:16:01.259 32408-32408/exacnv.examplekjbible D/TABLESQL: CREATE TABLE 'bible_fts_segments'(blockid INTEGER PRIMARY KEY, block BLOB)
	2019-08-30 12:16:01.259 32408-32408/exacnv.examplekjbible D/TABLESQL: CREATE TABLE 'bible_fts_segdir'(level INTEGER,idx INTEGER,start_block INTEGER,leaves_end_block INTEGER,end_block INTEGER,root BLOB,PRIMARY KEY(level, idx))

 - Note that only the three tables bible, metadata and the VIRTUAL table bible_fts are created directly by the conversion. bible_fts_?? tables are generated by the FTS3 module.


and
	
	2019-08-30 12:16:01.627 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Dumping a maximum of 5 objects from 31036.
	2019-08-30 12:16:01.627 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Object as from class exacnv.examplekjbible.Bible String represntation isexacnv.examplekjbible.Bible@32ad3a8
	2019-08-30 12:16:01.627 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Object as from class exacnv.examplekjbible.Bible String represntation isexacnv.examplekjbible.Bible@1a97ec1
	2019-08-30 12:16:01.627 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Object as from class exacnv.examplekjbible.Bible String represntation isexacnv.examplekjbible.Bible@40ff666
	2019-08-30 12:16:01.628 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Object as from class exacnv.examplekjbible.Bible String represntation isexacnv.examplekjbible.Bible@c32da7
	2019-08-30 12:16:01.628 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Object as from class exacnv.examplekjbible.Bible String represntation isexacnv.examplekjbible.Bible@12d8254
	2019-08-30 12:16:01.653 32408-32408/exacnv.examplekjbible W/CursorWindow: Window is full: requested allocation 242 bytes, free space 129 bytes, window size 2097152 bytes
	2019-08-30 12:16:01.765 32408-32408/exacnv.examplekjbible W/CursorWindow: Window is full: requested allocation 48 bytes, free space 26 bytes, window size 2097152 bytes
	2019-08-30 12:16:01.845 32408-32408/exacnv.examplekjbible W/CursorWindow: Window is full: requested allocation 48 bytes, free space 2 bytes, window size 2097152 bytes
	2019-08-30 12:16:01.959 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Dumping a maximum of 5 objects from 31036.
	2019-08-30 12:16:01.959 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Object as from class exacnv.examplekjbible.Bible_fts String represntation isexacnv.examplekjbible.Bible_fts@f0e45fd
	2019-08-30 12:16:01.959 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Object as from class exacnv.examplekjbible.Bible_fts String represntation isexacnv.examplekjbible.Bible_fts@11512f2
	2019-08-30 12:16:01.959 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Object as from class exacnv.examplekjbible.Bible_fts String represntation isexacnv.examplekjbible.Bible_fts@aeafd43
	2019-08-30 12:16:01.959 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Object as from class exacnv.examplekjbible.Bible_fts String represntation isexacnv.examplekjbible.Bible_fts@40a6fc0
	2019-08-30 12:16:01.959 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Object as from class exacnv.examplekjbible.Bible_fts String represntation isexacnv.examplekjbible.Bible_fts@4fe44f9
	2019-08-30 12:16:01.963 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Dumping a maximum of 5 objects from 3.
	2019-08-30 12:16:01.963 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Object as from class exacnv.examplekjbible.Metadata String represntation isexacnv.examplekjbible.Metadata@add6c3e
	2019-08-30 12:16:01.963 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Object as from class exacnv.examplekjbible.Metadata String represntation isexacnv.examplekjbible.Metadata@d362a9f
	2019-08-30 12:16:01.963 32408-32408/exacnv.examplekjbible D/OBJECTDUMP: Object as from class exacnv.examplekjbible.Metadata String represntation isexacnv.examplekjbible.Metadata@706c7ec

- The CursorWindow full messages just indicating the the CursorWindow couldn't hold the row that was being added as it was larger than the estimated row size and it would have been added to the next population of the CursorWindow.

The Chinook Database is similar other than that the 1 row as previously mentioned cannot be copied.














































































