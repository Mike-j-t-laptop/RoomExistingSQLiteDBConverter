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

public class EntityIndexAdapter extends ArrayAdapter {

    private static int color_warning_low, color_warning_high, color_normal;

    public EntityIndexAdapter(@NonNull Context context, ArrayList<IndexInfo> ii) {
        super(context, 0,ii);
        color_normal = context.getResources().getColor(R.color.colorEntityListBackground);
        color_warning_low = context.getResources().getColor(R.color.colorTextWarningLow);
        color_warning_high = context.getResources().getColor(R.color.colorTextWariningHigh);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.entity_index_item,parent,false);
        }
        TextView indexname = listItemView.findViewById(R.id.index_name);
        TextView tblname = listItemView.findViewById(R.id.table_name);
        TextView columns = listItemView.findViewById(R.id.column_list);
        TextView where_clause = listItemView.findViewById(R.id.where_clause);
        TextView unique_flag = listItemView.findViewById(R.id.unique_flag);
        TextView sql = listItemView.findViewById(R.id.sql);

        IndexInfo currentII = (IndexInfo) getItem(position);
        indexname.setText(currentII.getIndexName());
        tblname.setText(currentII.getTableName());
        StringBuilder sb = new StringBuilder();
        for (IndexColumnInfo ici: currentII.getColumns()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(ici.getColumnName());
        }
        columns.setText(sb.toString());
        where_clause.setText(currentII.getWhereClause());
        unique_flag.setText(String.valueOf(currentII.isUnique()));
        sql.setText(currentII.getSQL());
        /*
        TableInfo currentTI = (TableInfo) getItem(position);
        TextView tblname = listItemView.findViewById(R.id.table);
        TextView col_count = listItemView.findViewById(R.id.column_count);
        TextView fkey_count = listItemView.findViewById(R.id.fkey_count);
        TextView idx_count = listItemView.findViewById(R.id.idx_count);
        TextView trg_count = listItemView.findViewById(R.id.trigger_count);
        TextView primary_key_list = listItemView.findViewById(R.id.primary_keys);
        TextView sql = listItemView.findViewById(R.id.table_sql);

        if ((currentTI != null ? currentTI.getTableName() : null) != null) {
            tblname.setText(currentTI.getTableName());
        }
        if (currentTI.isRoomTable()) {
            tblname.setBackgroundColor(color_warning_high);
        } else {
            tblname.setBackgroundColor(color_normal);
        }
        col_count.setText(String.valueOf(currentTI.getColumns().size()));
        fkey_count.setText(String.valueOf(currentTI.getForeignKeyList().size()));
        idx_count.setText(String.valueOf(currentTI.getIndexCount()));
        trg_count.setText(String.valueOf(currentTI.getTriggerCount()));
        StringBuilder sb = new StringBuilder("Primary Keys:=");
        for (String s: currentTI.getPrimaryKeyList()) {
            sb.append(" ").append(s);
        }
        primary_key_list.setText(sb.toString());
        if (currentTI.getPrimaryKeyList().size() < 1) {
            primary_key_list.setBackgroundColor(color_warning_high);
        }
        sql.setText(currentTI.getSQL());
        */
        return listItemView;
    }
}
