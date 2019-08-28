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

### 3. Allows some tailoring of the Conversion

The Convert section allows :-

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
The following is the result of clicking the last (the database file **RoomAssetConversion.db,** which is located in the RoomDBConverterDBSource folder) :-

![Example 002](https://i.imgur.com/ohe6W3x.png)



# Testing

Limited testing has been undertaken. Initally the Chinook Database was used.

The chinnok database has been converted successfully.
However, there is a persistent issue in that 1 row is ignored in the employee table. 
This is because the employee table has a Foreign key, that references the employee table and the 
top-most employee is defined as not reporting to anyone by it's reportto value being null.

Although this is valid/usable in SQLite, Room insists that parent's are indexed and have the NOT NULL attribute.
As such the table cannot be created to be used by room and include this row.
The converted database has 15606 rows (out of 15607 rows for all tables).












