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

public class EntityTriggerAdapter extends ArrayAdapter {
    private static int color_warning_low, color_warning_high, color_normal;

    public EntityTriggerAdapter(@NonNull Context context, ArrayList<TriggerInfo> tri) {
        super(context, 0,tri);
        color_normal = context.getResources().getColor(R.color.colorEntityListBackground);
        color_warning_low = context.getResources().getColor(R.color.colorTextWarningLow);
        color_warning_high = context.getResources().getColor(R.color.colorTextWariningHigh);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.entity_trigger_item,parent,false);
        }
        TextView triggername = listItemView.findViewById(R.id.trigger_name);
        TextView triggertablename = listItemView.findViewById(R.id.trigger_tablename);
        TextView triggerSQL = listItemView.findViewById(R.id.trigger_sql);


        TriggerInfo currentTRI = (TriggerInfo) getItem(position);
        triggername.setText(currentTRI.getTriggerName());
        triggertablename.setText(currentTRI.getTriggerTable());
        triggerSQL.setText(currentTRI.getTriggerSQL());
        return listItemView;
    }
}