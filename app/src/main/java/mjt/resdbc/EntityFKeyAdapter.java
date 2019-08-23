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

public class EntityFKeyAdapter extends ArrayAdapter {
    private static int color_warning_low, color_warning_high, color_normal;

    public EntityFKeyAdapter(@NonNull Context context, ArrayList<ForeignKeyInfo> fi) {
        super(context, 0,fi);
        color_normal = context.getResources().getColor(R.color.colorEntityListBackground);
        color_warning_low = context.getResources().getColor(R.color.colorTextWarningLow);
        color_warning_high = context.getResources().getColor(R.color.colorTextWariningHigh);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.entity_foreignkey_item,parent,false);
        }
        TextView fkeyparenttable = listItemView.findViewById(R.id.fkey_parenttablename);
        TextView fkeychildtable = listItemView.findViewById(R.id.fkey_childtablename);
        TextView fkeyparentcolumns = listItemView.findViewById(R.id.fkey_parentcolumnnames);
        TextView fkeychildcolumns = listItemView.findViewById(R.id.fkey_childcolumnnames);
        TextView fkeyonupdateaction = listItemView.findViewById(R.id.fkey_onupdateaction);
        TextView fkeyondeleteaction = listItemView.findViewById(R.id.fkey_ondeleteaction);
        TextView fkeydefferrable = listItemView.findViewById(R.id.fkey_deferrableflag);

        ForeignKeyInfo currentFI = (ForeignKeyInfo) getItem(position);
        fkeychildtable.setText(currentFI.getTableName());
        fkeyparenttable.setText(currentFI.getParentTableName());
        fkeychildcolumns.setText(currentFI.getTableName());
        StringBuilder sb = new StringBuilder();
        for (String s: currentFI.getParentColumnNames()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(s);
        }
        fkeyparentcolumns.setText(sb.toString());
        sb = new StringBuilder();
        for (String s: currentFI.getChildColumnNames()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(s);
        }
        fkeychildcolumns.setText(sb.toString());
        fkeyonupdateaction.setText(currentFI.getOnUpdateAction());
        fkeyondeleteaction.setText(currentFI.getOnDeleteAction());
        fkeydefferrable.setText(String.valueOf(currentFI.isDeferable()));
        return listItemView;
    }
}