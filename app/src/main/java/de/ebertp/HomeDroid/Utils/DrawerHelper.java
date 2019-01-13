package de.ebertp.HomeDroid.Utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.ebertp.HomeDroid.Model.HMDrawerItem;
import de.ebertp.HomeDroid.R;

public class DrawerHelper {

    public static List<HMDrawerItem> getDrawerItems(Context ctx) {
        if (PreferenceHelper.isChildProtectionOn(ctx)) {
            return Collections.singletonList(new HMDrawerItem(11, R.string.quick_access, R.drawable.menu_quickaccess));
        }

        ArrayList<HMDrawerItem> items = new ArrayList<HMDrawerItem>();

        items.add(new HMDrawerItem(10, R.string.recents, R.drawable.menu_history));
        items.add(new HMDrawerItem(11, R.string.quick_access, R.drawable.menu_quickaccess));
        items.add(new HMDrawerItem(6, R.string.floor_plan, R.drawable.menu_floorplan));
        items.add(new HMDrawerItem(1, R.string.favs, R.drawable.menu_fav, true));
        items.add(new HMDrawerItem(0, R.string.rooms, R.drawable.menu_room));
        items.add(new HMDrawerItem(3, R.string.categories, R.drawable.menu_cat));
        items.add(new HMDrawerItem(2, R.string.variables, R.drawable.menu_var));
        items.add(new HMDrawerItem(4, R.string.scripts, R.drawable.menu_scripts));
        items.add(new HMDrawerItem(7, R.string.notifications, R.drawable.menu_notifications, true));

        if (Util.isSpeechSupported(ctx)) {
            items.add(new HMDrawerItem(8, R.string.speech_commands, R.drawable.menu_speech));
        }

        items.add(new HMDrawerItem(9, R.string.history, R.drawable.menu_history));
        items.add(new HMDrawerItem(5, R.string.virtual_remote, R.drawable.menu_remote));
        items.add(new HMDrawerItem(12, R.string.webcams, R.drawable.menu_cam));
        return items;
    }
}
