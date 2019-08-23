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

public class EntityViewAdapter extends ArrayAdapter {

    private static int color_warning_low, color_warning_high, color_normal;

    public EntityViewAdapter(@NonNull Context context, ArrayList<ViewInfo> vi) {
        super(context, 0,vi);
        color_normal = context.getResources().getColor(R.color.colorEntityListBackground);
        color_warning_low = context.getResources().getColor(R.color.colorTextWarningLow);
        color_warning_high = context.getResources().getColor(R.color.colorTextWariningHigh);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.entity_view_item,parent,false);
        }
        TextView viewname = listItemView.findViewById(R.id.view_name);
        TextView viewSQL = listItemView.findViewById(R.id.view_sql);


        ViewInfo currentVI = (ViewInfo) getItem(position);
        viewname.setText(currentVI.getViewName());
        viewSQL.setText(currentVI.getViewSQL());
        return listItemView;
    }
}