package de.ebertp.HomeDroid.Model;

public class HMLayerItem {

    int rowId, iseId, type;
    Integer posX, posY;
    String name;

    public HMLayerItem(int rowId, int iseId, int type, Integer posX, Integer posY, String name) {
        this.rowId = rowId;
        this.iseId = iseId;
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.name = name;
    }

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public int getIseId() {
        return iseId;
    }

    public void setIseId(int iseId) {
        this.iseId = iseId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Integer getPosX() {
        return posX;
    }

    public void setPosX(Integer posX) {
        this.posX = posX;
    }

    public Integer getPosY() {
        return posY;
    }

    public void setPosY(Integer posY) {
        this.posY = posY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
