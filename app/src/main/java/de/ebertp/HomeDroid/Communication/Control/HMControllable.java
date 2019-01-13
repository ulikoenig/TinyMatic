package de.ebertp.HomeDroid.Communication.Control;

import java.io.Serializable;

public class HMControllable implements Serializable {
    private static final long serialVersionUID = 1L;
    public int rowId;
    public String name;
    public String value;
    public String value_list;
    public Enum<HmType> type;
    public Integer datapointId;

    //hotfix for LayerActivity
    private int layerItemId;

    public HMControllable(int rowId, String name, HmType type) {
        this.rowId = rowId;
        this.name = name;
        this.type = type;
    }

    public HMControllable(int rowId, String name, String value, HmType type) {
        this.rowId = rowId;
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public HMControllable(int rowId, String name, String value, HmType type, Integer datapointId) {
        this.rowId = rowId;
        this.name = name;
        this.value = value;
        this.type = type;
        this.datapointId = datapointId;
    }

    public HMControllable(int rowId, String name, String value, String value_list, HmType type) {
        super();
        this.rowId = rowId;
        this.name = name;
        this.value = value;
        this.value_list = value_list;
        this.type = type;
    }


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue_list() {
        return value_list;
    }

    public void setValue_list(String value_list) {
        this.value_list = value_list;
    }

    public Enum<HmType> getType() {
        return type;
    }

    public void setType(Enum<HmType> type) {
        this.type = type;
    }

    public Integer getDatapointId() {
        return datapointId;
    }

    public void setDatapointId(int datapointId) {
        this.datapointId = datapointId;
    }

    public int getLayerItemId() {
        return layerItemId;
    }

    public void setLayerItemId(int layerItemId) {
        this.layerItemId = layerItemId;
    }
}
