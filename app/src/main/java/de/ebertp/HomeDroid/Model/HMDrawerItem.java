package de.ebertp.HomeDroid.Model;

public class HMDrawerItem {

    private int id;
    private Integer nameRes, picRes;
    private boolean drawBorderBelow;

    public HMDrawerItem(int id, Integer nameRes, Integer picRes, boolean drawBorderBelow) {
        this.id = id;
        this.nameRes = nameRes;
        this.picRes = picRes;
        this.drawBorderBelow = drawBorderBelow;
    }

    public HMDrawerItem(int id, Integer nameRes, Integer picRes) {
        this.id = id;
        this.nameRes = nameRes;
        this.picRes = picRes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getNameRes() {
        return nameRes;
    }

    public void setNameRes(Integer nameRes) {
        this.nameRes = nameRes;
    }

    public Integer getPicRes() {
        return picRes;
    }

    public void setPicRes(Integer picRes) {
        this.picRes = picRes;
    }

    public boolean isDrawBorderBelow() {
        return drawBorderBelow;
    }

    public void setDrawBorderBelow(boolean drawBorderBelow) {
        this.drawBorderBelow = drawBorderBelow;
    }
}
