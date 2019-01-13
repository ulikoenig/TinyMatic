package de.ebertp.HomeDroid.Model;

public class HMScript extends HMChannel {

    private String timestamp;

    public HMScript(int rowId, String name, String value, boolean isVisible, boolean isOperate, String timestamp) {
        super(rowId, 1, name, "HM-Program", value, isVisible, isOperate);
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
