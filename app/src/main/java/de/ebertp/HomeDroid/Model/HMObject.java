package de.ebertp.HomeDroid.Model;

public abstract class HMObject implements Comparable<HMObject> {

    public int rowId;
    private Integer mSortOrder;
    private int mHidden;

    protected HMObject() {

    }

    public HMObject(int rowId) {
        this.rowId = rowId;
    }

    public int compareTo(HMObject another) {
        // return getName().compareTo(another.getName());
        return 0;
    }

    public Integer getSortOrder() {
        return mSortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        mSortOrder = sortOrder;
    }

    public abstract String getName();

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public int getHidden() {
        return mHidden;
    }

    public void setHidden(Integer mHidden) {
        this.mHidden = mHidden;
    }

    @Override
    public String toString() {
        return getName();
    }

}
