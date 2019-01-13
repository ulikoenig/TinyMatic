package de.ebertp.HomeDroid.Communication.Control;

public class HmDatapoint {

    int ise_id, channel_id, value_type;
    String name, point_type, value, timestamp;

    public HmDatapoint(int ise_id, int channel_id, int value_type, String name, String type, String value, String timestamp) {
        this.ise_id = ise_id;
        this.channel_id = channel_id;
        this.value_type = value_type;
        this.name = name;
        this.point_type = type;
        this.value = value;
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
