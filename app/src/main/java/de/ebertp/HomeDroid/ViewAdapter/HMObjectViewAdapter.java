package de.ebertp.HomeDroid.ViewAdapter;

import android.widget.BaseAdapter;

import java.util.List;

import de.ebertp.HomeDroid.Model.HMObject;

public abstract class HMObjectViewAdapter extends BaseAdapter {

    public abstract java.util.List<HMObject> getObjects();

    public abstract void setObjects(List<HMObject> objects);

}
