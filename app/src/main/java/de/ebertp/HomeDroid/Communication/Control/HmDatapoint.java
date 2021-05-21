package de.ebertp.HomeDroid.Communication.Control;

public class HmDatapoint {

    int ise_id, channel_id, value_type;
    Integer operations;
    String name, point_type, value, value_unit, timestamp;

    public HmDatapoint(int ise_id, int channel_id, int value_type, String name, String type, String value, String value_unit, Integer operations, String timestamp) {
        this.ise_id = ise_id;
        this.channel_id = channel_id;
        this.value_type = value_type;
        this.name = name;
        this.point_type = type;
        this.value = value;
        this.value_unit = value_unit;
        this.operations = operations;
        this.timestamp = timestamp;
    }

    public int getIse_id() {
        return ise_id;
    }

    public int getChannel_id() {
        return channel_id;
    }

    public int getValue_type() {
        return value_type;
    }

    public Integer getOperations() {
        return operations;
    }

    public String getValue_unit() {
        return value_unit;
    }

    public String getName() {
        return name;
    }

    public String getPoint_type() {
        return point_type;
    }

    public String getValue() {
        return value;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
