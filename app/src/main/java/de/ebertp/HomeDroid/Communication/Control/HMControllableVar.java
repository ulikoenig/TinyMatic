package de.ebertp.HomeDroid.Communication.Control;


public class HMControllableVar extends HMControllable {
    private static final long serialVersionUID = 1L;
    public double min, max;
    public boolean isPercent;
    public String unit;
    public double doubleValue;

    public HMControllableVar(int rowId, String name, String value, HmType type, double min, double max, boolean isPercent, String unit,
                             double doubleValue, Integer datapointId) {
        super(rowId, name, value, type);

        this.min = min;
        this.max = max;
        this.isPercent = isPercent;
        this.unit = unit;
        this.doubleValue = doubleValue;
        this.datapointId = datapointId;

    }

}
