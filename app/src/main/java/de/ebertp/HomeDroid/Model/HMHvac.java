package de.ebertp.HomeDroid.Model;

public class HMHvac {

    public enum Type {
        MODE, VANE, SPEED, ROOM
    }

    Type type;
    int value;
    int datapointId;

    public HMHvac(Type type, int value, int datapointId) {
        this.type = type;
        this.value = value;
        this.datapointId = datapointId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getDatapointId() {
        return datapointId;
    }

    public void setDatapointId(int datapointId) {
        this.datapointId = datapointId;
    }


}
