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

public class EntityTableAdapter extends ArrayAdapter {

    private static int color_warning_low, color_warning_high, color_normal;

    public EntityTableAdapter(@NonNull Context context, ArrayList<TableInfo> ti) {
        super(context, 0,ti);
        color_normal = context.getResources().getColor(R.color.colorEntityListBackground);
        color_warning_low = context.getResources().getColor(R.color.colorTextWarningLow);
        color_warning_high = context.getResources().getColor(R.color.colorTextWariningHigh);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.entity_table_item,parent,false);
        }

        TableInfo currentTI = (TableInfo) getItem(position);
        TextView tblname = listItemView.findViewById(R.id.table);
        TextView encltblname = listItemView.findViewById(R.id.enclosedtable);
        TextView col_count = listItemView.findViewById(R.id.column_count);
        TextView fkey_count = listItemView.findViewById(R.id.fkey_count);
        TextView idx_count = listItemView.findViewById(R.id.idx_count);
        TextView trg_count = listItemView.findViewById(R.id.trigger_count);
        TextView primary_key_list = listItemView.findViewById(R.id.primary_keys);
        TextView sql = listItemView.findViewById(R.id.table_sql);

        tblname.setBackgroundColor(color_normal);
        if ((currentTI != null ? currentTI.getTableName() : null) != null) {
            tblname.setText(currentTI.getTableName());
        }
        if (currentTI.isRoomTable()) {
            tblname.setBackgroundColor(color_warning_high);
            tblname.setText(currentTI.getTableName() + "\n\t" + getContext().getResources().getString(R.string.entitywarningroommastertable));
        }
        if (currentTI.isVirtualTable()) {
            tblname.setBackgroundColor(color_warning_high);
            tblname.setText(currentTI.getTableName() + "\n\t" + getContext().getResources().getString(R.string.entitytablevirtualtable));
        }

        encltblname.setText(currentTI.getEnclosedTableName());
        col_count.setText(String.valueOf(currentTI.getColumns().size()));
        fkey_count.setText(String.valueOf(currentTI.getForeignKeyList().size()));
        idx_count.setText(String.valueOf(currentTI.getIndexCount()));
        trg_count.setText(String.valueOf(currentTI.getTriggerCount()));
        StringBuilder sb = new StringBuilder();
        for (String s: currentTI.getPrimaryKeyList()) {
            if (sb.length()>0) {
                sb.append("\n");
            }
            sb.append(s);
        }
        primary_key_list.setText(sb.toString());
        if (currentTI.getPrimaryKeyList().size() < 1) {
            primary_key_list.setBackgroundColor(color_warning_high);
            primary_key_list.setText(sb.toString() + "\n\t" + getContext().getResources().getString(R.string.entitywarningnoprimarykey));
        } else {
            primary_key_list.setBackgroundColor(color_normal);
        }
        sql.setText(currentTI.getSQL());
        return listItemView;
    }
}
