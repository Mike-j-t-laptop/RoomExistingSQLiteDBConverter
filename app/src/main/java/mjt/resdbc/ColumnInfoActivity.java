package mjt.resdbc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColumnInfoActivity extends AppCompatActivity implements Serializable {
    Button mDone, mApplyChanges;
    ColumnInfo mCI;
    TextView mColumnName, mAlternativeColumnName,
            mOwningTableName,
            mType,mDerivedType,mFinalType,mObjectElementType,
            mNotNull,mUnique,mRowidAlias,mAutoIncrement,mDefault;
    EditText mColumnNameEdit,mAlternativeColumnNameEdit;
    Spinner mFinalTypeSelect, mObjectElementTypeSelect;
    CheckBox mNotNullCheckbox;
    ArrayList<String> mApplicableObjectElementType;
    List<String> mApplicableTypes = Arrays.asList(SQLiteConstants.ROOM_AFFINITIES);
    ArrayAdapter mObjectElementSA, mFinalTypesSA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_column_info);
        mColumnName = this.findViewById(R.id.column_name);
        mColumnNameEdit = this.findViewById(R.id.edit_column_name);
        mAlternativeColumnName = this.findViewById(R.id.altcolumn_name);
        mAlternativeColumnNameEdit = this.findViewById(R.id.edit_altcolumn_name);
        mOwningTableName = this.findViewById(R.id.owningtable);
        mType = this.findViewById(R.id.column_type);
        mDerivedType = this.findViewById(R.id.derived_type);
        mFinalType = this.findViewById(R.id.final_type);
        mFinalTypeSelect = this.findViewById(R.id.final_type_spinner);
        mObjectElementType = this.findViewById(R.id.objectelement_type);
        mObjectElementTypeSelect = this.findViewById(R.id.objectelement_type_spinner);
        mNotNull = this.findViewById(R.id.column_notnull);
        mNotNullCheckbox = this.findViewById(R.id.checkbox_notnull);
        mUnique = this.findViewById(R.id.column_unique);
        mRowidAlias = this.findViewById(R.id.column_rowidalias);
        mAutoIncrement = this.findViewById(R.id.column_autoincrementcoded);
        mDefault = this.findViewById(R.id.column_default_value);
        mDone = this.findViewById(R.id.donebutton);
        mApplyChanges = this.findViewById(R.id.applychangesbutton);
        mCI = (ColumnInfo) this.getIntent().getSerializableExtra(MainActivity.INTENTKEY_COLUMNINFO);

        mColumnName.setText(mCI.getColumnName());
        mColumnNameEdit.setText(mCI.getColumnName());
        mAlternativeColumnName.setText(mCI.getAlternativeColumnName());
        mAlternativeColumnNameEdit.setText(mCI.getAlternativeColumnName());
        mOwningTableName.setText(mCI.getOwningTable());
        mType.setText(mCI.getColumnType());
        mDerivedType.setText(mCI.getDerivedTypeAffinity());
        mFinalType.setText(mCI.getFinalTypeAffinity());
        mObjectElementType.setText(mCI.getObjectElementType());
        mNotNull.setText(String.valueOf(mCI.isNotNull()));
        mNotNullCheckbox.setChecked(mCI.isNotNull());
        mUnique.setText(String.valueOf(mCI.isUnique()));
        mRowidAlias.setText(String.valueOf(mCI.isRowidAlias()));
        mAutoIncrement.setText(String.valueOf(mCI.isAutoIncrementCoded()));
        mDefault.setText(mCI.getDefaultValue());
        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            }
        });
        mApplyChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedelement = (String) mObjectElementTypeSelect.getSelectedItem();
                ColumnInfo changes = new ColumnInfo(
                        mColumnNameEdit.getText().toString(),
                        mAlternativeColumnNameEdit.getText().toString(),
                        mOwningTableName.getText().toString(),
                        mType.getText().toString(),
                        mDerivedType.getText().toString(),
                        (String) mFinalTypeSelect.getSelectedItem(),
                        (String) mObjectElementTypeSelect.getSelectedItem(),
                        mNotNullCheckbox.isChecked(),
                        mCI.getCID(),
                        mCI.getPrimaryKeyPosition(),mDefault.getText().toString(),mCI.isUnique(),mCI.isRowidAlias(),mCI.isAutoIncrementCoded(),mCI.getColumnCreateSQL()
                );
                Intent intent = new Intent();
                intent.putExtra(MainActivity.INTENTKEY_CHANGEDCOLUMNINFO,changes);
                intent.putExtra(MainActivity.INTENTKEY_COLUMNINFO,mCI);
                setResult(Activity.RESULT_OK,intent);
                finish();
            }
        });
        manageFinalTypeSpinner();
    }

    private void manageFinalTypeSpinner() {
        if (mFinalTypesSA == null) {
            mFinalTypesSA = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,android.R.id.text1,mApplicableTypes);
            mFinalTypeSelect.setAdapter(mFinalTypesSA);
        }
        int currentSelection =0;
        for (int i=0; i < mApplicableTypes.size(); i++) {
            if (mApplicableTypes.get(i).equals(mFinalType.getText().toString())) {
                currentSelection = i;
                break;
            }
        }
        mFinalTypeSelect.setSelection(currentSelection);
        mFinalTypeSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                manageObjectElementTypeSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void manageObjectElementTypeSpinner() {
        if (mApplicableObjectElementType == null) mApplicableObjectElementType = new ArrayList<>();
        if (mFinalTypeSelect.getSelectedItem().equals(SQLiteConstants.AFFINITY_INTEGER)) {
            mApplicableObjectElementType.clear();
            mApplicableObjectElementType.addAll(Arrays.asList(SQLiteConstants.ROOMTYPES_INTEGER));
        }
        if (mFinalTypeSelect.getSelectedItem().equals(SQLiteConstants.AFFINITY_TEXT)) {
            mApplicableObjectElementType.clear();
            mApplicableObjectElementType.addAll(Arrays.asList(SQLiteConstants.ROOMTYPES_TEXT));
        }
        if (mFinalTypeSelect.getSelectedItem().equals(SQLiteConstants.AFFINITY_REAL)) {
            mApplicableObjectElementType.clear();
            mApplicableObjectElementType.addAll(Arrays.asList(SQLiteConstants.ROOMTYPES_REAL));
        }
        if (mFinalTypeSelect.getSelectedItem().equals(SQLiteConstants.AFFINITY_BLOB)) {
            mApplicableObjectElementType.clear();
            mApplicableObjectElementType.addAll(Arrays.asList(SQLiteConstants.ROOMTYPES_BLOB));
        }
        if (mObjectElementSA == null) {
            mObjectElementSA = new ArrayAdapter(this,android.R.layout.simple_list_item_1,android.R.id.text1,mApplicableObjectElementType);
            mObjectElementTypeSelect.setAdapter(mObjectElementSA);
        } else {
            mObjectElementSA.notifyDataSetChanged();
        }
        int currentselection = 0;
        for (int i=0; i < mApplicableObjectElementType.size(); i++) {
            if (mApplicableObjectElementType.get(i).equals(mObjectElementType.getText().toString())) {
                currentselection = i;
                break;
            }
        }
        mObjectElementTypeSelect.setSelection(currentselection);
    }
}