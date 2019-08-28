# RoomExistingSQLiteDBConverter
App to convert an Exisiting non-Room database for Room, generating the @Entity and @Room code as well as converting the database.

The App initially searches the External Publc Storage (often called sdcard (but not on the an sdcard)) for databases, 
except any in the reserved directory RoomDBConverterDBConversions and lists the databases (includes checking the header).
Note encrypted databases are not handled.

Note when first run, permission will be requested. If not provided no Database will be listed.

Selecting a Database file, display information about the Database, and initially the Tables.
Clicking Tables, Columns, Indexes, FK, Triggers or Views, switches to display information about the respetive componenets.

Warnings/changes are highlighted in the component. 
Orange is more informational.
(e.g. type affinity VARCHAR(100) will be changed to TEXT (it's derived type affinity as per SQLite)).
Red indicates a more prominent change or issue.
(e.g. a type affinity of DATETIME is changed to TEXT (Final) as the derived affinity is NUMERIC which is not supported by Room).

In addition to the Database Information being display along with component information. Another section is displayed that caters for
the conversion.

This allows the default conversion sub-directory ( all conversions are placed into the RoomDBConverterDBConversions directory),
this being the directory specififc to the conversion it defaults to the database name prefixed with Convert_.

The package name can be specified, by default it is blank, if it is known. 
Specifying the package is advantageous as it will be endcoded in the resultant code.

Safemode, when checked, encloses Database component names in `` when creating the converted components. 
If unchecked then Database component names are not enclosed and can result in components not being created.
Note currently it is expected that components adhere to java naming/usage conventions 
(an ultra-safe maode may be introcuded that prefixes component names).

The Entity Dao Sub-directoryies allow the code to be generated in seperate directories. By default the directory used is java.
Note a single directory for both simplifies copying the code.

The CONVERT button wil undertake the Conversion of the database and the generation of the java code. 
A dialog will appear detailing the results. 
An ideal conversion is one where the copied rows matches the original rows and there are no warnings.
However, there may be accetable/managable exceptions.

# Testing

Limited testing has been undertaken. Initally the Chinook Database was used.

The chinnok database has been converted successfully.
However, there is a persistent issue in that 1 row is ignored in the employee table. 
This is because the employee table has a Foreign key, that references the employee table and the 
top-most employee is defined as not reporting to anyone by it's reportto value being null.

Although this is valid/usable in SQLite, Room insists that parent's are indexed and have the NOT NULL attribute.
As such the table cannot be created to be used by room and include this row.
The converted database has 15606 rows (out of 15607 rows for all tables).












