package mjt.resdbc;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Serializable, PermissionGranted, FileListBuilt {

    public static final String BASECONVERTDIRECTORY = "RoomDBConverterDBConversions";

    public static final int REQUESTCODE_TABLEINFO = 99;
    public static final int REQUESTCODE_COLUMNINFO = 98;
    public static final String INTENTKEY_TABLEINFO = "ik_tableinfo";
    public static final String INTENTKEY_COLUMNINFO = "ik_columninfo";
    public static final String INTENTKEY_CHANGEDCOLUMNINFO = "ik_changedcolumninfo";

    private static final int ENTITY_TABLE = 0;
    private static final int ENTITY_COLUMN = 1;
    private static final int ENTITY_INDEX = 2;
    private static final int ENTITY_TRIGGER = 3;
    private static final int ENTITY_VIEW = 4;
    private static final int ENTITY_FOREIGNKEY = 5;
    private static String[] ENTITYTITLE;

    int mCurrentEntity = ENTITY_TABLE;

    Context mContext;
    Button mConvert, mRefreshFileList;
    ListView mDBEntityLisView, mDBFilesListView;
    LinearLayout mDBInfoHdr, mDBInfo,  mDBPathInfoHdr, mConvertSection;
    TextView mSelectedDBInfoHdr, mDBName, mDBVersion,  mDBDiskSize, mDBPath,
            mDBTablesHdr, mDBColumnsHdr, mDBIndexesHdr, mDBFrgnKeysHdr, mDBTriggersHdr,mDBViewsHdr,
            mDBTables,mDBColumns, mDBIndexes, mDBFrgnKeys, mDBTriggers, mDBViews, mDBEntitiesListHdr;
    EditText mConversionPackageNameEditText,
            mConversionDirectoryEditText, mConversionEntityDirectoryEditText, mConversionDaoDirectoryEditText;
    CheckBox mSafeMode;

    ArrayList<FileEntry> mDBFiles;
    ArrayAdapter<FileEntry> mDBFilesAA;
    EntityTableAdapter mDBTablesAA;
    EntityColumnAdapter mDBColumnsAA;
    EntityIndexAdapter mDBIndexesAA;
    EntityFKeyAdapter mDBForeignKeysAA;
    EntityTriggerAdapter mDBTriggersAA;
    EntityViewAdapter mDBViewsAA;

    ArrayList<PreExistingFileDBInspect> mPEFDBIList;
    ArrayList<TableInfo> mCurrentTables;
    ArrayList<ForeignKeyInfo> mCurrentForeignKeys;
    ArrayList<ColumnInfo> mCurrentColumns;
    ArrayList<IndexInfo> mCurrentIndexes;
    ArrayList<TriggerInfo> mCurrentTriggers;
    ArrayList<ViewInfo> mCurrentViews;
    PreExistingFileDBInspect mCurrentPEFDBI;

    String mConversionDirectory = "",
            mEntityDirectory = "java",
            mDAODirectory = mEntityDirectory,
            mConversionPackageName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        ENTITYTITLE = this.getResources().getStringArray(R.array.entitytypes);
        setContentView(R.layout.activity_main);
        mRefreshFileList = this.findViewById(R.id.refresh);
        mConvert = this.findViewById(R.id.convert);
        mDBFilesListView = this.findViewById(R.id.dbfileslist);
        (mDBEntityLisView = this.findViewById(R.id.dbentitieslist)).setVisibility(View.GONE);
        (mSelectedDBInfoHdr = this.findViewById(R.id.selectedassetheading)).setVisibility(View.GONE);
        (mDBInfoHdr = this.findViewById(R.id.dbinfohdr)).setVisibility(View.GONE);
        (mDBInfo = this.findViewById(R.id.dbinfo)).setVisibility(View.GONE);
        mDBName = this.findViewById(R.id.dbname);
        mDBVersion = this.findViewById(R.id.dbversion);
        mDBDiskSize = this.findViewById(R.id.dbdisksize);
        mDBTables = this.findViewById(R.id.dbtablecount);
        mDBPath = this.findViewById(R.id.dbpath);
        mDBColumns = this.findViewById(R.id.dbcolumncount);
        mDBIndexes = this.findViewById(R.id.dbindexcount);
        mDBFrgnKeys = this.findViewById(R.id.dbforeignkeycount);
        mDBTriggers = this.findViewById(R.id.dbtriggercount);
        mDBViews = this.findViewById(R.id.dbviewcount);
        mDBTablesHdr = this.findViewById(R.id.dbtablecounthdr);
        mDBColumnsHdr = this.findViewById(R.id.dbtablecolumncounthdr);
        mDBIndexesHdr = this.findViewById(R.id.dbindexcounthdr);
        mDBFrgnKeysHdr = this.findViewById(R.id.dbforeignkeycounthdr);
        mDBTriggersHdr = this.findViewById(R.id.dbtriggercounthdr);
        mDBViewsHdr = this.findViewById(R.id.dbviewcounthdr);
        (mDBPathInfoHdr = this.findViewById(R.id.pathinfosection)).setVisibility(View.GONE);
        (mDBEntitiesListHdr = this.findViewById(R.id.dbentitieslisthdr)).setVisibility(View.GONE);
        (mConvertSection = this.findViewById(R.id.convert_linear_layout)).setVisibility(View.GONE);
        mConversionDirectoryEditText = this.findViewById(R.id.conversion_directory);
        mConversionEntityDirectoryEditText = this.findViewById(R.id.conversion_entity_directory);
        mConversionDaoDirectoryEditText = this.findViewById(R.id.conversion_dao_directory);
        mConversionPackageNameEditText = this.findViewById(R.id.projectpackage);
        mSafeMode = this.findViewById(R.id.safemode);

        ExternalStoragePermissions.verifyStoragePermissions(this);

        //Handle refreshing the list of databases
        mRefreshFileList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageDBFilesListView();
                mConversionPackageNameEditText.setText("");
            }
        });
        manageDBFilesListView();
        manageDatabaseInformationListeners();
        manageConvertButton();
        manageConversionEditTexts();
    }

    /**
     * Manage the Conversion EditText's by adding on text changed listeners
     */
    private void manageConversionEditTexts() {
        mConversionDirectoryEditText.addTextChangedListener(new CustomTextWatcher());
        mConversionEntityDirectoryEditText.addTextChangedListener(new CustomTextWatcher());
        mConversionDaoDirectoryEditText.addTextChangedListener(new CustomTextWatcher());
    }

    /**
     * Manage the Convert Button that converts the currently selected database if EditTexts contain appropriate input
     */
    private void manageConvertButton() {

        mConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConversionDirectoryEditText.getText().toString().length() < 1) {
                    Toast.makeText(v.getContext(),getResources().getString(R.string.convert_directory_empty),Toast.LENGTH_SHORT).show();
                    mConversionDirectoryEditText.requestFocus();
                    return;
                }
                if (mConversionEntityDirectoryEditText.getText().toString().length() < 1) {
                    Toast.makeText(v.getContext(),getResources().getString(R.string.convert_entity_directory_empty),Toast.LENGTH_SHORT).show();
                    mConversionEntityDirectoryEditText.requestFocus();
                    return;
                }
                if (mConversionDaoDirectoryEditText.getText().toString().length() < 1) {
                    Toast.makeText(v.getContext(),getResources().getText(R.string.convert_dao_directory_empty),Toast.LENGTH_SHORT).show();
                    mConversionDaoDirectoryEditText.requestFocus();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Handle safemode
                        String encloserStart = "`", encloserEnd = encloserStart;
                        if (!mSafeMode.isChecked()) {
                            encloserStart = "";
                            encloserEnd = encloserStart;
                        }
                        // Handle package name
                        String packageName = ""; // assume not suplied
                        if (mConversionPackageNameEditText.getText() != null && mConversionPackageNameEditText.getText().toString().length() > 0) {
                            packageName = mConversionPackageNameEditText.getText().toString();
                            if (!packageName.endsWith(";")) {
                                packageName = packageName + ";";
                            }
                        }
                        // Do the conversion
                        showConversionResults(
                                (
                                        ConvertPreExistingDatabaseToRoom.Convert(
                                                mCurrentPEFDBI, BASECONVERTDIRECTORY + File.separator +
                                                        mConversionDirectoryEditText.getText().toString(),
                                                mConversionEntityDirectoryEditText.getText().toString(),
                                                mDAODirectory,ConvertPreExistingDatabaseToRoom.MESSAGELEVEL_ERROR,
                                                encloserStart, encloserEnd, //<<<<<<<<<< the enclosers typically should be empty or `
                                                packageName
                                        )
                                                == 0
                                )
                        );
                    }
                }).start();
            }
        });
    }

    /**
     * manages the visibility of the convert Button, disabling it if EditText's have no data
     * note overkill in association with the checks done in manageConvertButton
     */
    private void setConvertButtonFocusability() {
        int cdetLength = mConversionDirectoryEditText.getText().toString().length();
        int cedetLength = mConversionEntityDirectoryEditText.getText().toString().length();
        int cddetLength = mConversionDaoDirectoryEditText.getText().toString().length();
        if (cdetLength > 0 && cedetLength > 0 && cddetLength > 0){
            mConvert.setVisibility(View.VISIBLE);
        } else {
            mConvert.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Manage the File List (SQLite Databases in external public storage),
     * Part 1 (File List retrieval) as run on non UI thread
     */
    private void manageDBFilesListView() {
        if (mDBFiles != null) {
            mDBFiles.clear();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                fileListBuilt(RetrieveDBFiles.getFiles());
            }
        }).start();
    }

    /**
     * Manage the FileList Listview,
     * Part 2 setup the adapter and listview if not done,
     * otherwise refresh the ListView
     */
    private void manageDBFilesListViewAfterRetrieve() {
        if (mDBFiles == null) return;
        if (mDBFilesAA == null) {
            mDBFilesAA = new ArrayAdapter<>(this,R.layout.filelist_layout,R.id.filepath,mDBFiles);
            mDBFilesListView.setAdapter(mDBFilesAA);
            mDBFilesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    handleSelectedFile((mDBFilesAA.getItem(position)).getmFile());
                }
            });
        } else {
            mDBFilesAA.notifyDataSetChanged();
        }
    }

    /**
     * Manage the Database information headers and actual data to add on click
     * listeners that result in the respective adapter being used for the database list
     * information (i.e. switch between the various list tables, columns, indexes, triggers and views)
     */
    private void manageDatabaseInformationListeners() {

        mDBTables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_TABLE;
                selectAdapter();
            }
        });
        mDBTablesHdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_TABLE;
                selectAdapter();
            }
        });
        mDBColumns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_COLUMN;
                selectAdapter();
            }
        });
        mDBColumnsHdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_COLUMN;
                selectAdapter();
            }
        });
        mDBIndexes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_INDEX;
                selectAdapter();
            }
        });
        mDBIndexesHdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_INDEX;
                selectAdapter();
            }
        });
        mDBFrgnKeys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_FOREIGNKEY;
                selectAdapter();
            }
        });
        mDBFrgnKeysHdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_FOREIGNKEY;
                selectAdapter();
            }
        });
        mDBTriggers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_TRIGGER;
                selectAdapter();
            }
        });
        mDBTriggersHdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_TRIGGER;
                selectAdapter();
            }
        });
        mDBViews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_VIEW;
                selectAdapter();
            }
        });
        mDBViewsHdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEntity = ENTITY_VIEW;
                selectAdapter();
            }
        });

    }

    /**
     * Manage the database information listable data (tables, columns, indexes, triggers and views)
     * first; call the setDBEntityList method to refresh/build the underlying data
     * second; if the adapters are null (should all be in the same state) then instantiate the
     * adapters
     * third; if already instantiated then notify the adapters that the underlying data may have changed
     * fourth; call the selectAdapter method to tie the respective adapter to the ListView
     */
    private void manageDBEntityListView() {
        setDBEntityLists();
        if (mDBTablesAA == null) {
            mDBTablesAA = new EntityTableAdapter(this, mCurrentTables);
            mDBColumnsAA = new EntityColumnAdapter(this,mCurrentColumns);
            mDBIndexesAA = new EntityIndexAdapter(this,mCurrentIndexes);
            mDBForeignKeysAA = new EntityFKeyAdapter(this,mCurrentForeignKeys);
            mDBTriggersAA = new EntityTriggerAdapter(this,mCurrentTriggers);
            mDBViewsAA = new EntityViewAdapter(this,mCurrentViews);
            manageDBEntityListListViewListeners();
        } else {
            mDBTablesAA.notifyDataSetChanged();
            mDBColumnsAA.notifyDataSetChanged();
            mDBIndexesAA.notifyDataSetChanged();
            mDBTriggersAA.notifyDataSetChanged();
            mDBViewsAA.notifyDataSetChanged();
            mDBForeignKeysAA.notifyDataSetChanged();

        }
        selectAdapter();
    }

    /**
     * Manage the EntityListView Listeners, currently longclicking a column when viewing
     * columns will allow some column attributes to be changed.
     */
    private void manageDBEntityListListViewListeners() {
        mDBEntityLisView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Object x = parent.getItemAtPosition(position);
                if (x instanceof TableInfo) {
                    return true;
                    //Intent intent = new Intent(view.getContext(),TableInfoActivity.class);
                    //intent.putExtra(INTENTKEY_TABLEINFO,(TableInfo)x);
                    //startActivityForResult(intent,REQUESTCODE_TABLEINFO);
                }
                //TODO Not Really supported as yet, needs work to properly rename SQlite entities that use a renamed column
                if (x instanceof ColumnInfo) {
                    //Intent intent = new Intent(view.getContext(),ColumnInfoActivity.class);
                    //intent.putExtra(INTENTKEY_COLUMNINFO,(ColumnInfo)x);
                    //startActivityForResult(intent,REQUESTCODE_COLUMNINFO);
                }
                return true ;
            }
        });
    }

    /**
     * Tie the respective adapter to the database information list ListView
     */
    private void selectAdapter() {
        switch (mCurrentEntity) {
            case ENTITY_COLUMN:
                mDBEntityLisView.setAdapter(mDBColumnsAA);
                mDBEntitiesListHdr.setText(ENTITYTITLE[ENTITY_COLUMN]);
                break;
            case ENTITY_INDEX:
                mDBEntityLisView.setAdapter(mDBIndexesAA);
                mDBEntitiesListHdr.setText(ENTITYTITLE[ENTITY_INDEX]);
                break;
            case ENTITY_TRIGGER:
                mDBEntityLisView.setAdapter(mDBTriggersAA);
                mDBEntitiesListHdr.setText(ENTITYTITLE[ENTITY_TRIGGER]);
                break;
            case ENTITY_VIEW:
                mDBEntityLisView.setAdapter(mDBViewsAA);
                mDBEntitiesListHdr.setText(ENTITYTITLE[ENTITY_VIEW]);
                break;
            case ENTITY_FOREIGNKEY:
                mDBEntityLisView.setAdapter(mDBForeignKeysAA);
                mDBEntitiesListHdr.setText(ENTITYTITLE[ENTITY_FOREIGNKEY]);
                break;
            default:
                mDBEntityLisView.setAdapter(mDBTablesAA);
                mDBEntitiesListHdr.setText(ENTITYTITLE[ENTITY_TABLE]);
        }
    }

    /**
     * Handle selection of a file from the Listview listing the SQLiteDatabase files
     * @param f     the selected file
     */
    public void handleSelectedFile(File f) {
        mConversionPackageNameEditText.setText("");
        if (mPEFDBIList == null) {
            mPEFDBIList = new ArrayList<>();
        }
        boolean alreadyExits = false;
        int i = 0;
        for (PreExistingFileDBInspect p: mPEFDBIList) {
            if (p.getDatabasePath().equals(f.getPath())) {
                alreadyExits = true;
                break;
            }
            i++;
        }
        if (alreadyExits) {
            mCurrentPEFDBI = mPEFDBIList.get(i);
        } else {
            mPEFDBIList.add(new PreExistingFileDBInspect(this,f));
            mCurrentPEFDBI = mPEFDBIList.get(mPEFDBIList.size()-1);
        }
        mConversionDirectory = getResources().getString(R.string.convert_main_directory_prefix) + mCurrentPEFDBI.getDatabaseName();
        mConvertSection.setVisibility(View.VISIBLE);
        mConversionDirectoryEditText.setText(mConversionDirectory);
        mConversionEntityDirectoryEditText.setText(mEntityDirectory);
        mConversionDaoDirectoryEditText.setText(mDAODirectory);
        mSelectedDBInfoHdr.setVisibility(View.VISIBLE);
        mDBInfo.setVisibility(View.VISIBLE);
        mDBInfoHdr.setVisibility(View.VISIBLE);
        mDBPathInfoHdr.setVisibility(View.VISIBLE);
        mDBEntityLisView.setVisibility(View.VISIBLE);
        mDBEntitiesListHdr.setVisibility(View.VISIBLE);
        mDBName.setText(mCurrentPEFDBI.getDatabaseName());
        mDBVersion.setText(String.valueOf(mCurrentPEFDBI.getDatabaseVersion()));
        mDBDiskSize.setText(String.valueOf(mCurrentPEFDBI.getDatabaseDiskSize()));
        mDBTables.setText(String.valueOf(mCurrentPEFDBI.getTableCount()));
        mDBColumns.setText(String.valueOf(mCurrentPEFDBI.getColumnCount()));
        mDBIndexes.setText(String.valueOf(mCurrentPEFDBI.getIndexCount()));
        mDBFrgnKeys.setText(String.valueOf(mCurrentPEFDBI.getForeignKeyCount()));
        mDBTriggers.setText(String.valueOf(mCurrentPEFDBI.getTriggerCount()));
        mDBViews.setText(String.valueOf(mCurrentPEFDBI.getViewCount()));
        mDBPath.setText(mCurrentPEFDBI.getDatabasePath());
        manageDBEntityListView();
    }

    /**
     * Rebuild the Entity Lists according to the current PEFDBI
     */
    private void setDBEntityLists() {
        if (mCurrentTables == null) {
            mCurrentTables = new ArrayList<>();
        }
        mCurrentTables.clear();
        for (TableInfo ti: mCurrentPEFDBI.getTableInfo()) {
            TableInfo newti = new TableInfo(ti.getTableName(),ti.getSQL(),
                    ti.getColumns(), ti.getColumnLookup(),
                    ti.getForeignKeyList(), ti.getPrimaryKeyList(), ti.getPrimaryKeyListAlternativeNames(),
                    ti.getReferencelevel(),ti.getIndexCount(),ti.getTriggerCount(),ti.isRowid(),ti.isRoomTable()
            );
            mCurrentTables.add(newti);
        }

        if (mCurrentColumns == null) {
            mCurrentColumns = new ArrayList<>();
        }
        mCurrentColumns.clear();
        for (ColumnInfo ci: mCurrentPEFDBI.getColumnInfo()) {
            ColumnInfo newci = new ColumnInfo(
                    ci.getColumnName(), ci.getAlternativeColumnName(),
                    ci.getOwningTable(),
                    ci.getColumnType(),ci.getDerivedTypeAffinity(),ci.getFinalTypeAffinity(),ci.getObjectElementType(),
                    ci.isNotNull(),
                    ci.getCID(),
                    ci.getPrimaryKeyPosition(),
                    ci.getDefaultValue(),
                    ci.isUnique(),
                    ci.isRowidAlias(),
                    ci.isAutoIncrementCoded(),
                    ci.getColumnCreateSQL(),ci.getOriginalColumnName(),ci.getOriginalAlternativeColumnName()
            );
            mCurrentColumns.add(newci);
        }

        if (mCurrentIndexes == null) {
            mCurrentIndexes = new ArrayList<>();
        }
        mCurrentIndexes.clear();
        for (IndexInfo ii: mCurrentPEFDBI.getIndexInfo()) {
            ArrayList<IndexColumnInfo> newici = new ArrayList<>();
            for (IndexColumnInfo ici: ii.getColumns()) {
                newici.add(new IndexColumnInfo(ici.getColumnName(),ici.getColumnIndexRank(),ici.getColumnTableRank()));
            }
            IndexInfo newii = new IndexInfo(ii.getIndexName(),ii.getSQL(),ii.isUnique(),ii.getTableName(),newici,ii.getWhereClause());
            mCurrentIndexes.add(newii);
        }

        if (mCurrentForeignKeys == null) {
            mCurrentForeignKeys = new ArrayList<>();
        }
        mCurrentForeignKeys.clear();
        for(TableInfo ti: mCurrentPEFDBI.getTableInfo()) {
            ArrayList<ForeignKeyInfo> newfki = new ArrayList<>();
            for (ForeignKeyInfo ifki: ti.getForeignKeyList()) {
                newfki.add(new ForeignKeyInfo(ti.getTableName(),ifki.getParentTableName(),ifki.getChildColumnNames(),ifki.getParentColumnNames(),ifki.getOnUpdate(),ifki.getOnDelete(),ifki.isDeferable()));
            }
            mCurrentForeignKeys.addAll(newfki);
        }

        if (mCurrentTriggers == null) {
            mCurrentTriggers = new ArrayList<>();
        }
        mCurrentTriggers.clear();
        for (TriggerInfo tri: mCurrentPEFDBI.getTriggerInfo()) {
            TriggerInfo newtri = new TriggerInfo(tri.getTriggerName(),tri.getTriggerTable(),tri.getTriggerSQL());
            mCurrentTriggers.add(newtri);
        }
        if (mCurrentViews == null) {
            mCurrentViews = new ArrayList<>();
        }
        mCurrentViews.clear();
        for (ViewInfo vi: mCurrentPEFDBI.getViewInfo()) {
            ViewInfo newvi = new ViewInfo(vi.getViewName(),vi.getViewTable(),vi.getViewSQL());
            mCurrentViews.add(newvi);
        }
    }

    /**
     * Handle returns from other Activities
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUESTCODE_COLUMNINFO:
                    ColumnInfo changes = (ColumnInfo) data.getSerializableExtra(INTENTKEY_CHANGEDCOLUMNINFO);
                    ColumnInfo original = (ColumnInfo) data.getSerializableExtra(INTENTKEY_COLUMNINFO);
                    applyColumnChanges(changes,original);
                    break;
                case REQUESTCODE_TABLEINFO:
                    break;
            }
        }
        if (requestCode == RESULT_CANCELED) {
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    /**
     * Appl column changes made by the ColumnActivity
     * @param changes
     * @param original
     */
    private void applyColumnChanges(ColumnInfo changes, ColumnInfo original) {
        boolean changes_applied = false;
        if (
                original.getColumnName().equals(changes.getColumnName())
                        && original.getAlternativeColumnName().equals(changes.getAlternativeColumnName())
                        && original.getFinalTypeAffinity().equals(changes.getFinalTypeAffinity())
                        && original.getObjectElementType().equals(changes.getObjectElementType())
                        && original.isNotNull() == changes.isNotNull()
        ) {
            return;
        }
        for (TableInfo ti: mCurrentPEFDBI.getTableInfo()) {
            if (ti.getTableName().equals(original.getOwningTable())) {
                for (ColumnInfo ci: ti.getColumns()) {
                    if (ci.getColumnName().equals(original.getColumnName())) {
                        ci.setColumnName(changes.getColumnName());
                        ci.setAlternativeColumnName(changes.getAlternativeColumnName());
                        ci.setFinalTypeAffinity(changes.getFinalTypeAffinity());
                        ci.setObjectElementType(changes.getObjectElementType());
                        ci.setNotNull(changes.isNotNull());
                        changes_applied = true;
                    }
                }
            }
        }
        if (changes_applied) {
            manageDBEntityListView();
        }
    }

    /**
     * Display a dialog detailing the result of the conversion
     * @param issues
     */
    public void showConversionResults(final boolean issues) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String preamble = "Copied a total of " +
                        String.valueOf(ConvertPreExistingDatabaseToRoom.getTotalCopiedRows() +
                                " rows, " +
                                "out of " +
                                String.valueOf(ConvertPreExistingDatabaseToRoom.getTotalOriginalRows())
                                + "\nDouble check Original Count is " + String.valueOf(ConvertPreExistingDatabaseToRoom.getTor()) +
                                " Copied count is " + String.valueOf(ConvertPreExistingDatabaseToRoom.getTcr())
                                + ".\n\n"
                        );
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                if (issues) {
                    alertDialog.setTitle(getResources().getString(R.string.convert_result_not_ok));
                } else {
                    alertDialog.setTitle(getResources().getString(R.string.convert_result_ok));
                }
                alertDialog.setTitle("Result");
                alertDialog.setMessage(preamble + ConvertPreExistingDatabaseToRoom.getMessagesAsString());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
    }

    /**
     * handle permission having been granted,
     * i.e. refresh the FileList as without permission none would be listed
     */
    @Override
    public void permissionHasBeenGranted() {
        manageDBFilesListView();
    }

    /**
     * Handle the files aving been retrieved
     * i.e. goto part2 od managing the FileLise ListView
     * @param fileList
     */
    @Override
    public void fileListBuilt(ArrayList<FileEntry> fileList) {
        if (mDBFiles == null) {
            mDBFiles = new ArrayList<>();
        }
        mDBFiles.clear();
        mDBFiles.addAll(fileList);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                manageDBFilesListViewAfterRetrieve();
            }
        });
    }

    /**
     * TextWatcher for the Conversion EditTexts,
     * all just call the setConvertFocusability method
     */
    private class CustomTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            setConvertButtonFocusability();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        manageDBFilesListView();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        for (PreExistingFileDBInspect p: mPEFDBIList) {
            p.closeInspectionDatabase();
        }
        super.onDestroy();
    }
}
