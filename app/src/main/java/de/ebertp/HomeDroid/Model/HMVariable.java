package de.ebertp.HomeDroid.Model;

public class HMVariable extends HMChannel {
    public String variable, unit, vartype, subtype, value_list;
    public int min, max;
    private String valueName0;
    private String valueName1;
    private String timestamp;

    public HMVariable(int rowId, String name, String variable, String value, String value_list, int min, int max, String unit, String vartype, String subtype, boolean isVisible, boolean isOperate, String valueName0, String valueName1, String timestamp) {
        super(rowId, 0, name, "HM-Variable", "", isVisible, isOperate);
        this.variable = variable;
        this.min = min;
        this.value = value;
        this.value_list = value_list;
        this.max = max;
        this.unit = unit;
        this.vartype = vartype;
        this.subtype = subtype;
        this.valueName0 = valueName0;
        this.valueName1 = valueName1;
        this.timestamp = timestamp;
    }

    public int compareTo(HMObject another) {
        return getName().compareTo(another.getName());
    }

    public String getName() {
        return name;
    }

    public String getVariable() {
        return variable;
    }

    public String getUnit() {
        return unit;
    }

    public String getVartype() {
        return vartype;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getValue_list() {
        return value_list;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public String getValueName0() {
        return valueName0;
    }

    public String getValueName1() {
        return valueName1;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
