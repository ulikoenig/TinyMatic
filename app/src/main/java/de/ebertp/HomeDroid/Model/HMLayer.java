package de.ebertp.HomeDroid.Model;

public class HMLayer extends HMObject {

    String name, background;


    public HMLayer(int rowId, String name, String background) {
        super(rowId);
        this.name = name;
        this.background = background;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    @Override
    public String toString() {
        return name;
    }


}
