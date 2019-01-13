package de.ebertp.HomeDroid.Model;

import com.j256.ormlite.field.DatabaseField;

import java.sql.SQLException;

import de.ebertp.HomeDroid.HomeDroidApp;

public class HMObjectSettings {

    @DatabaseField(id = true)
    int rowId;

    @DatabaseField
    String customName;

    //unused, db fuckup
    @DatabaseField
    int hidden;

    public HMObjectSettings() {
    }

    public HMObjectSettings(int rowId, String customName, int hidden) {
        this.rowId = rowId;
        this.customName = customName;
        this.hidden = hidden;
    }

    public int getRowId() {
        return rowId;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public int getHidden() {
        return hidden;
    }

    public void setHidden(int hidden) {
        this.hidden = hidden;
    }

    public static HMObjectSettings getSettingsForId(int rowId) {
        try {
            return HomeDroidApp.db().getHmOjectSettingsDao().queryForId(rowId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
