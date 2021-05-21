package de.ebertp.HomeDroid.Model;

import de.ebertp.HomeDroid.Utils.Util;

public class HMChannel extends HMObject {

    public int channelIndex;
    public String name;
    public String type;
    public String value = "";
    private String address;
    private boolean isVisible;
    private boolean isOperate;

    public HMChannel(int rowId, String address, int channelIndex, String name, String type, boolean isVisible, boolean isOperate) {
        super(rowId);
        this.channelIndex = channelIndex;
        this.name = name;
        this.type = type;
        this.address = address;
        this.isVisible = isVisible;
        this.isOperate = isOperate;
    }

    public HMChannel(int rowId, int channelIndex, String name, String type, String value, boolean isVisible, boolean isOperate) {
        super(rowId);
        this.channelIndex = channelIndex;
        this.name = name;
        this.type = type;
        this.value = value;
        this.isVisible = isVisible;
        this.isOperate = isOperate;
    }

    public int compareTo(HMObject another) {
        return getName().compareTo(another.getName());
    }

    public String getName() {
        return name;
    }

    public int getChannelIndex() {
        return channelIndex;
    }

    public void setChannelIndex(int channelIndex) {
        this.channelIndex = channelIndex;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isOperate() {
        return isOperate;
    }

    public void setOperate(boolean isOperate) {
        this.isOperate = isOperate;
    }

    public boolean isIPDevice() {
        return Util.startsWithIgnoreCase(type, "HmIP-");
    }
}
