package de.ebertp.HomeDroid.ViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import de.ebertp.HomeDroid.R;

public class ImageAdapter extends BaseAdapter {
    private final LayoutInflater li;
    private Context context;

    Integer[] imageIDs = {
            R.drawable.icon1,
            R.drawable.icon2,
            R.drawable.icon3,
            R.drawable.icon4,
            R.drawable.icon5,
            R.drawable.icon6,
            R.drawable.icon7,
            R.drawable.icon8,
            R.drawable.icon9,
            R.drawable.icon10,
            R.drawable.icon11,
            R.drawable.icon12,
            R.drawable.icon13,
            R.drawable.icon14,
            R.drawable.icon15,
            R.drawable.icon16,
            R.drawable.icon17,
            R.drawable.icon18,
            R.drawable.icon19,
            R.drawable.icon_new1,
            R.drawable.icon_new2,
            R.drawable.icon_new3,
            R.drawable.icon_new4,
            R.drawable.icon_new5,
            R.drawable.icon_new6,
            R.drawable.icon_new7,
            R.drawable.icon_new8,
            R.drawable.icon_new9,
            R.drawable.icon_new10,
            R.drawable.icon_new11,
            R.drawable.icon_new12,
            R.drawable.icon_new13,
            R.drawable.icon_new14,
            R.drawable.icon_new15,
            R.drawable.icon_new16,
            R.drawable.icon_new17,
            R.drawable.icon_new18,
            R.drawable.icon_new19,
            R.drawable.icon_new20,
            R.drawable.icon_new21,
            R.drawable.icon_new22,
            R.drawable.icon_new23,
            R.drawable.icon_new24,
            R.drawable.icon_new25,
            R.drawable.icon_new26,
            R.drawable.icon_new27,
            R.drawable.icon_new28,
            R.drawable.icon_new29,
            R.drawable.icon_new30,
            R.drawable.icon_new31,
            R.drawable.icon_new32,
            R.drawable.icon_new33,
            R.drawable.icon_new34,
            R.drawable.icon_new35,
            R.drawable.icon_new36,
            R.drawable.icon_new37,
            R.drawable.icon_new38,
            R.drawable.icon_new39,
            R.drawable.icon_new40,
            R.drawable.icon_new41,
            R.drawable.icon_new42,
            R.drawable.old_icon1,
            R.drawable.old_icon2,
            R.drawable.old_icon3,
            R.drawable.old_icon4,
            R.drawable.old_icon5,
            R.drawable.old_icon6,
            R.drawable.old_icon7,
            R.drawable.old_icon8,
            R.drawable.old_icon9,
            R.drawable.old_icon10,
            R.drawable.old_icon11,
            R.drawable.old_icon12,
            R.drawable.old_icon13,
            R.drawable.old_icon14,
            R.drawable.old_icon15,
            R.drawable.old_icon16,
            R.drawable.old_icon17,
            R.drawable.old_icon18,
            R.drawable.old_icon19,
            R.drawable.metro_1,
            R.drawable.metro_10,
            R.drawable.metro_100,
            R.drawable.metro_101,
            R.drawable.metro_102,
            R.drawable.metro_103,
            R.drawable.metro_104,
            R.drawable.metro_105,
            R.drawable.metro_106,
            R.drawable.metro_107,
            R.drawable.metro_108,
            R.drawable.metro_109,
            R.drawable.metro_11,
            R.drawable.metro_110,
            R.drawable.metro_111,
            R.drawable.metro_112,
            R.drawable.metro_113,
            R.drawable.metro_114,
            R.drawable.metro_115,
            R.drawable.metro_116,
            R.drawable.metro_117,
            R.drawable.metro_118,
            R.drawable.metro_119,
            R.drawable.metro_12,
            R.drawable.metro_120,
            R.drawable.metro_121,
            R.drawable.metro_122,
            R.drawable.metro_123,
            R.drawable.metro_124,
            R.drawable.metro_125,
            R.drawable.metro_126,
            R.drawable.metro_127,
            R.drawable.metro_128,
            R.drawable.metro_129,
            R.drawable.metro_13,
            R.drawable.metro_130,
            R.drawable.metro_131,
            R.drawable.metro_132,
            R.drawable.metro_133,
            R.drawable.metro_134,
            R.drawable.metro_135,
            R.drawable.metro_14,
            R.drawable.metro_15,
            R.drawable.metro_16,
            R.drawable.metro_17,
            R.drawable.metro_18,
            R.drawable.metro_19,
            R.drawable.metro_2,
            R.drawable.metro_20,
            R.drawable.metro_21,
            R.drawable.metro_22,
            R.drawable.metro_23,
            R.drawable.metro_24,
            R.drawable.metro_25,
            R.drawable.metro_26,
            R.drawable.metro_27,
            R.drawable.metro_28,
            R.drawable.metro_29,
            R.drawable.metro_3,
            R.drawable.metro_30,
            R.drawable.metro_31,
            R.drawable.metro_32,
            R.drawable.metro_33,
            R.drawable.metro_34,
            R.drawable.metro_35,
            R.drawable.metro_36,
            R.drawable.metro_37,
            R.drawable.metro_38,
            R.drawable.metro_39,
            R.drawable.metro_4,
            R.drawable.metro_40,
            R.drawable.metro_41,
            R.drawable.metro_42,
            R.drawable.metro_43,
            R.drawable.metro_45,
            R.drawable.metro_46,
            R.drawable.metro_47,
            R.drawable.metro_48,
            R.drawable.metro_49,
            R.drawable.metro_5,
            R.drawable.metro_50,
            R.drawable.metro_51,
            R.drawable.metro_52,
            R.drawable.metro_53,
            R.drawable.metro_54,
            R.drawable.metro_55,
            R.drawable.metro_56,
            R.drawable.metro_57,
            R.drawable.metro_58,
            R.drawable.metro_59,
            R.drawable.metro_6,
            R.drawable.metro_60,
            R.drawable.metro_61,
            R.drawable.metro_62,
            R.drawable.metro_63,
            R.drawable.metro_64,
            R.drawable.metro_65,
            R.drawable.metro_66,
            R.drawable.metro_67,
            R.drawable.metro_68,
            R.drawable.metro_69,
            R.drawable.metro_7,
            R.drawable.metro_70,
            R.drawable.metro_71,
            R.drawable.metro_72,
            R.drawable.metro_73,
            R.drawable.metro_74,
            R.drawable.metro_75,
            R.drawable.metro_76,
            R.drawable.metro_77,
            R.drawable.metro_78,
            R.drawable.metro_79,
            R.drawable.metro_8,
            R.drawable.metro_80,
            R.drawable.metro_81,
            R.drawable.metro_82,
            R.drawable.metro_83,
            R.drawable.metro_84,
            R.drawable.metro_85,
            R.drawable.metro_86,
            R.drawable.metro_87,
            R.drawable.metro_88,
            R.drawable.metro_89,
            R.drawable.metro_9,
            R.drawable.metro_90,
            R.drawable.metro_91,
            R.drawable.metro_92,
            R.drawable.metro_93,
            R.drawable.metro_94,
            R.drawable.metro_95,
            R.drawable.metro_96,
            R.drawable.metro_97,
            R.drawable.metro_98,
            R.drawable.metro_99,
            R.drawable.icon_gahle1,
            R.drawable.icon_gahle2,
            R.drawable.icon_gahle3,
            R.drawable.icon_gahle4,
            R.drawable.icon_gahle5,
            R.drawable.icon_gahle6,
            R.drawable.icon_gahle7,
            R.drawable.icon_gahle8,
            R.drawable.icon_gahle9,
            R.drawable.icon_gahle10,
            R.drawable.icon_gahle11,
            R.drawable.icon_gahle12,
            R.drawable.icon_gahle13,
            R.drawable.icon_gahle14,
            R.drawable.icon_gahle15,
            R.drawable.icon_gahle16,
            R.drawable.icon_gahle17,
            R.drawable.icon_gahle18,
            R.drawable.icon_gahle19,
            R.drawable.icon_gahle20,
            R.drawable.icon_gahle21,
            R.drawable.icon_gahle22,
            R.drawable.icon_gahle23,
            R.drawable.icon_gahle24,
            R.drawable.icon_gahle25,
            R.drawable.icon_gahle26,
            R.drawable.icon_gahle27,
            R.drawable.icon_gahle28,
            R.drawable.icon_gahle29,
            R.drawable.icon_gahle30,
            R.drawable.icon_gahle31,
    };

    public ImageAdapter(Context ctx) {
        context = ctx;
        li = LayoutInflater.from(ctx);
    }

    //---returns the number of images---
    public int getCount() {
        return imageIDs.length;
    }

    //---returns the ID of an item---
    public Object getItem(int position) {
        return position;
    }

    public Integer getIconId(int position) throws Exception {
        return imageIDs[position];
    }

    public long getItemId(int position) {
        return position;
    }

    //---returns an ImageView view---
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = li.inflate(R.layout.icon_gallery_icon, null);
        }
        ((ImageView) convertView).setImageResource(imageIDs[position]);

        return convertView;
    }
}
