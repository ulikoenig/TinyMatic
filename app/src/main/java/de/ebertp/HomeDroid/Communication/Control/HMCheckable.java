package de.ebertp.HomeDroid.Communication.Control;

public class HMCheckable extends HMControllable {

    private static final long serialVersionUID = 1L;
    private final Integer falseIcon;
    private final Integer trueIcon;
    String falseValueName, trueValueName;

    public HMCheckable(int rowId, String name, String value, HmType type, String falseValueName, String trueValueName, Integer falseIcon, Integer trueIcon, Integer datapointId) {
        super(rowId, name, value, type);
        this.falseValueName = falseValueName;
        this.trueValueName = trueValueName;
        this.falseIcon = falseIcon;
        this.trueIcon = trueIcon;
        this.datapointId = datapointId;
    }

    public HMCheckable(int rowId, String name, String value, HmType type) {
        super(rowId, name, value, type);
        this.falseValueName = null;
        this.trueValueName = null;
        this.falseIcon = null;
        this.trueIcon = null;
    }

    public String getFalseValueName() {
        return falseValueName;
    }

    public String getTrueValueName() {
        return trueValueName;
    }

    public Integer getFalseIcon() {
        return falseIcon;
    }

    public Integer getTrueIcon() {
        return trueIcon;
    }
}
