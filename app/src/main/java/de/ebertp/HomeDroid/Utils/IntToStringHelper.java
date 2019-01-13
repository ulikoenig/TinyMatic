package de.ebertp.HomeDroid.Utils;

public class IntToStringHelper {

    public static int getIntFromString(String s) {
        if (s == null || s.length() == 0) {
            return -1;
        }
        return (int) Double.parseDouble(s);
    }

    public static String getNotNullString(String s) {
        if (s == null || s.length() == 0) {
            return " ";
        }
        return s;
    }
}
