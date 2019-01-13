package de.ebertp.HomeDroid.Model;

public class HMRoom extends HMObject {

    private String name;
    private String rawName;

    public HMRoom(int rowId, String name, String rawName) {
        super(rowId);
        this.name = name;
        this.rawName = rawName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRawName() {
        return rawName;
    }

    public void setRawName(String rawName) {
        this.rawName = rawName;
    }
}
