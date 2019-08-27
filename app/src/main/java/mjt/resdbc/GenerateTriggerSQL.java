package mjt.resdbc;

import java.util.ArrayList;
import java.util.List;

public class GenerateTriggerSQL {

    public static List<String> generateTriggerSQL(PreExistingFileDBInspect pefdbi, String encloserStart, String encloserEnd) {
        ArrayList<String> triggerSQLList = new ArrayList<>();
        for(TriggerInfo tri: pefdbi.getTriggerInfo()) {
            triggerSQLList.add(tri.getTriggerSQL());
        }
        return triggerSQLList;
    }
}
