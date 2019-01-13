package de.ebertp.HomeDroid.Model;

import java.util.Date;

public class HMNotification extends HMObject {

    public final String name;
    public final String type;
    public final Date timestamp;

    public HMNotification(int rowId, String name, String type, Date timestamp) {
        super(rowId);
        this.name = name;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Date getTimestamp() {
        return timestamp;
    }


}
