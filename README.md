# RoomExistingSQLiteDBConverter #

## What the App does. ##
App to convert an Exisiting non-Room database for Room, generating the **@Entity** and **@Dao** code as well as converting the database.

- *Andorid's ROOM expects the SQLite Database schema to adhere to it's rules. It is the **@Entity**'s that define the expected schema.*
- *As such comnverting the database is relatively simple in comparison to generating the correct **@Entity**'s, especially if the underlying rules are unknown; and hence why the App can be useful.* 
- *Android's ROOM does now provide a conversion utility, but it only converts the database, the task of creating the exact **@Entity**'s, is still required.*

That is the App produces 3 distinct sets of data :-

1. The Converted database.
2. The @Entity's, one per table in the underlying database.
3. @Dao's for the most basic operations (insert/update/delete and retrieve all), again oe per table.

  -  ***Note**, the generated code lacks the import statements (this is intentional, as they can vary). As such every generated file should be visited to resolve the imports (and if no package has been supplied the package statement as well).*

##An Overview of how the App works ##


### 1. Retrieves the databases

1. The App initially searches the External Publc Storage (often called sdcard (but not on the an sdcard)) for databases, 
except any in the reserved directory RoomDBConverterDBConversions and lists the databases (includes checking the header).
Note encrypted databases are not handled.

- **Note**, when first run, permission will be requested. If not provided no Database will be listed.
- The **REFRESH** button will refresh the list (e.g. after adding a database).

### 2. Inspects the selected database
2. Selecting a Database file, display information about the Database, and initially the Tables.
Clicking Tables, Columns, Indexes, FK, Triggers or Views, switches to display information about the respetive componenets.

1. Warnings/changes are highlighted in the component. 
  1. Orange is more informational.
(e.g. type affinity VARCHAR(100) will be changed to TEXT (it's derived type affinity as per SQLite)).
  1. Red indicates a more prominent change or issue.
(e.g. a type affinity of DATETIME is changed to TEXT (Final) as the derived affinity is NUMERIC which is not supported by Room).

### 3. Tailoring the Conversion output

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

- Conversion
 - Clicking the **CONVERT**- button will attempt to undertake the conversion.
 - A dialog appears with messages. 

#Example Usage (user guide)

##Initial Display##
When the App is first started **(after accepting permission)** then the database files found within the External Public Storage are displayed (if any).
- If permisssion is not granted then no files/database will be found as read access to the directory prevents any being found.
- If there are none then the database(s) to be converted should be copied into External Public Storage (typically this is sdcard aka storage/emulated/0, however it can be device dependant *(see [https://developer.android.com/training/data-storage/files](https://developer.android.com/training/data-storage/files))*

![Example 001](https://i.imgur.com/yBYcBdl.png)

Here you can see that 7 databases have been located in various directories.

##Selecting and Inspecting a Database##

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

###Tables View

Lists information about each table. If a table is a deviation from a usual table and has to be treated differently then the table name will be followed by relevant information.

####Deviations/notifications are :-

1. **Indicates a room master table, this will be omitted from the converted database.** (RED).
 1. ROOM manages the room_master table, it may lead to issues if this table were converted/copied. It is also likely that there is no neeed to convert such a database.
1. **Room requires all entities (tables) to have a primary key. Will generate primary key from all columns.** (RED).
2. **Indicates a virtual table, this will be omitted from the converted database.** (RED).
 1. Limited support is provided for VIRTUAL tables, that is only for FTS3 and FTS4. If FTS3 or FTS4.
 2. 
3. **USING supported module ????** (Orange) (where ???? is the module name, as above). 
 1. The VIRTUAL table will be converted. However, the other FTS tables, which are generated by the FTS module will be omitted as per the deviation/notification :-
4. **Indicates a FTS table, this will be generated, there will be no @Entity or @Dao files created for this table.** (RED)

###Columns View

Lists information about each column in the database. Notifications can appear with the respective attribute's value.

####Notifications are :-
1. **Indicates a room master table, this will be omitted from the converted database.** (RED - with the Table attribute's value).
1. **The specified type affinity is not usable in Room, the derived type will be used (if not NUMERIC).** (Orange -  with the Derived attribute's value).
 1. If the Type Affinity used when defining the original/source table is not one of the four supported by ROOM (TEXT, INTEGER, REAL or BLOB), then this notifications indicates that it will be changed.
1. **NUMERIC type detected. Using TEXT and therefore object member will be String.** (RED - with the FINAL attribute's value).
 1. Using **String** as the object type is the most easily converted option e.g. it could cater if DATETIME were the original type affinity with timestamps or date/timestrings.
1. **AUTOINCREMENT will be added.** (RED - with the Autoincrement Coded attribute's value).
 1. For automatically generated rowid's the **@Entity** includes **`@PrimaryKey(autoGenerate = true)`** and this requires that **`AUTOINCREMENT`** is coded (as well as `NOT NULL`).
1. **A default value has been detected. You may wish to set the object member to default to the value.** (RED - with the Default attribute's value). 

###Indexes, FK, Triggers and Views

These views simply provide the information.

1. Indexes will be created.
2. Foreign Keys will be coded at the TABLE LEVEL.
3. Triggers will be created using the SQL extracted from sqlite_master.
4. Views are currently ignored.

##Convert

Clicking the **CONVERT** button result in a dialog displaying the results of the conversion. 

For example when converting the **RoomAssetConversion.db** ([the Chinook Database](http://https://www.sqlitetutorial.net/sqlite-sample-database/)), the result is :-

![Example 003](https://i.imgur.com/8eTr1jC.png)

That is bar the WARNING the conversion was successfull.

- The WARNING is due to 1 row not being copied in the **employees** table. 
- This is because the database has an internal foreign key on the **reportsTo** column and that NULL is used to indicate reporting to nobody. This in conjunction with ROOM requiring that the indexes for Foreign Keys must have NOT NULL.

If there are issues then the dialog should display such issues.


# Testing

Limited testing has been undertaken. Initally the Chinook Database was used.

The chinnok database has been converted successfully.
However, there is a persistent issue in that 1 row is ignored in the employee table. 
This is because the employee table has a Foreign key, that references the employee table and the 
top-most employee is defined as not reporting to anyone by it's reportto value being null.

Although this is valid/usable in SQLite, Room insists that parent's are indexed and have the NOT NULL attribute.
As such the table cannot be created to be used by room and include this row.
The converted database has 15606 rows (out of 15607 rows for all tables).
