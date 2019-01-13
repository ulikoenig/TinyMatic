package de.ebertp.HomeDroid.Model;

import java.util.Date;

public class HMProtocol extends HMObject {

    public final String name;
    public final String value;
    public final long timestamp;

    public Date timestampDate;

    public HMProtocol(String name, String value, long timestamp) {
        super(0);
        this.name = name;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public Date getTimestamp() {
        if (timestampDate == null) {
            timestampDate = new Date(getTimeStamp());
        }
        return timestampDate;
    }


}
