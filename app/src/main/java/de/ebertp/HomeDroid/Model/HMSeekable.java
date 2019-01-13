package de.ebertp.HomeDroid.Model;

import java.io.Serializable;

public class HMSeekable implements Serializable {

    private static final long serialVersionUID = 1L;
    public int rowId;
    public double value;
    public String name, unit;
    public int min, max;
    public boolean isPercent;
    public boolean needsSeekbar;

    public HMSeekable(int id, double value, int min, int max, String unit, String name, boolean isPercent, boolean needsSeekbar) {
        this.rowId = id;
        this.value = value;
        this.min = min;
        this.max = max;
        this.unit = unit;
        this.name = name;
        this.isPercent = isPercent;
        this.needsSeekbar = needsSeekbar;
    }
}
