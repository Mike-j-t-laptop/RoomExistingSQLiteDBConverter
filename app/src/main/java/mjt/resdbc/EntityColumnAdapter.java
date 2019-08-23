package mjt.resdbc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class EntityColumnAdapter extends ArrayAdapter {

    private static int color_warning_low, color_warning_high, color_normal;

    public EntityColumnAdapter(@NonNull Context context, ArrayList<ColumnInfo> ci) {
        super(context, 0,ci);
        color_warning_low = context.getResources().getColor(R.color.colorTextWarningLow);
        color_warning_high = context.getResources().getColor(R.color.colorTextWariningHigh);
        color_normal = context.getResources().getColor(R.color.colorEntityListBackground);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.entity_column_item,parent,false);
        }

        ColumnInfo currentCI = (ColumnInfo) getItem(position);
        TextView colname = listItemView.findViewById(R.id.column);
        TextView owningtable = listItemView.findViewById(R.id.owningtable);
        TextView col_type = listItemView.findViewById(R.id.column_type);
        TextView derived_type = listItemView.findViewById(R.id.derived_type);
        TextView final_type = listItemView.findViewById(R.id.final_type);
        TextView objectelement_type = listItemView.findViewById(R.id.objectelement_type);
        TextView alt_column = listItemView.findViewById(R.id.altcolumn_name);
        TextView notnull = listItemView.findViewById(R.id.column_notnull);
        TextView unique = listItemView.findViewById(R.id.column_unique);
        TextView rowidalias = listItemView.findViewById(R.id.column_rowidalias);
        TextView autoinccoded = listItemView.findViewById(R.id.column_autoincrementcoded);
        TextView defaultvalue = listItemView.findViewById(R.id.column_default_value);
        TextView sql = listItemView.findViewById(R.id.column_sql);

        colname.setText(currentCI.getColumnName());
        owningtable.setText(currentCI.getOwningTable());
        col_type.setText(currentCI.getColumnType());
        derived_type.setText(currentCI.getDerivedTypeAffinity());
        final_type.setText(currentCI.getFinalTypeAffinity());
        objectelement_type.setText(currentCI.getObjectElementType());
        alt_column.setText(currentCI.getAlternativeColumnName());
        notnull.setText(String.valueOf(currentCI.isNotNull()));
        unique.setText(String.valueOf(currentCI.isUnique()));
        rowidalias.setText(String.valueOf(currentCI.isRowidAlias()));
        autoinccoded.setText(String.valueOf(currentCI.isAutoIncrementCoded()));
        defaultvalue.setText(currentCI.getDefaultValue());

        col_type.setBackgroundColor(color_normal);
        derived_type.setBackgroundColor(color_normal);
        defaultvalue.setBackgroundColor(color_normal);
        final_type.setBackgroundColor(color_normal);
        owningtable.setBackgroundColor(color_normal);
        rowidalias.setBackgroundColor(color_normal);
        autoinccoded.setBackgroundColor(color_normal);
        if (!currentCI.getColumnName().equals(currentCI.getOriginalColumnName())) {
            colname.setBackgroundColor(color_warning_high);
            colname.setText(currentCI.getColumnName() + " (CHANGED from " + currentCI.getOriginalColumnName() + ")");
        } else {
            colname.setBackgroundColor(color_normal);
        }
        if (!currentCI.getAlternativeColumnName().equals(currentCI.getOriginalAlternativeColumnName())) {
            alt_column.setBackgroundColor(color_warning_high);
            alt_column.setText(currentCI.getAlternativeColumnName() + "(CHANGED from " + currentCI.getOriginalAlternativeColumnName() + ")");
        } else {
            alt_column.setBackgroundColor(color_normal);
        }
        if (currentCI.getOwningTable().toUpperCase().equals(SQLiteConstants.ROOM_MASTER_TABLE.toUpperCase())) {
            owningtable.setBackgroundColor(color_warning_high);
            owningtable.setText(currentCI.getOwningTable() + "\n\t" + getContext().getResources().getString(R.string.entitywarningroommastertable));
        }
        if (currentCI.isRowidAlias() && !currentCI.isAutoIncrementCoded()) {
            autoinccoded.setBackgroundColor(color_warning_high);
            autoinccoded.setText((String.valueOf(currentCI.isAutoIncrementCoded())) + "\n\t" + getContext().getResources().getString(R.string.entitywarningautoincrementadd));
        }
        if (!currentCI.getColumnType().equals(currentCI.getDerivedTypeAffinity())) {
            derived_type.setBackgroundColor(color_warning_low);
            derived_type.setText(currentCI.getDerivedTypeAffinity() + "\n\t" + getContext().getResources().getString(R.string.entitywarningderivedtype));
        }
        if (currentCI.getDerivedTypeAffinity().equals("NUMERIC")) {
            final_type.setBackgroundColor(color_warning_high);
            final_type.setText(currentCI.getFinalTypeAffinity() + "\n\t" + getContext().getResources().getString(R.string.entitywarningnumerictype));
        }
        if (currentCI.getDefaultValue().length() > 0) {
            defaultvalue.setBackgroundColor(color_warning_high);
            defaultvalue.setText(currentCI.getDefaultValue() + "\n\t" + getContext().getResources().getString(R.string.entitywarningdefaultvalue));
        }
        sql.setText(currentCI.getColumnCreateSQL());
        return listItemView;
    }
}
