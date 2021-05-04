package de.ebertp.HomeDroid.Utils;

import android.widget.ImageView;

import de.ebertp.HomeDroid.Model.HMRoom;
import de.ebertp.HomeDroid.R;

public class IconUtil {


    public static boolean setDefaultIcon(HMRoom hmRoom, ImageView iv) {
        String rawName = hmRoom.getRawName();

        if (rawName.startsWith("${")) {
            rawName = rawName.replace("${", "");
            rawName = rawName.replace("}", "");
        }

        int resId = -1;
        switch (rawName) {
            case "roomLivingRoom":
            case "Wohnzimmer":
                resId = R.drawable.icon1;
                break;
            case "roomKitchen":
            case "Küche":
                resId = R.drawable.icon5;
                break;
            case "roomBedroom":
            case "Schlafzimmer":
                resId = R.drawable.icon6;
                break;
            case "roomChildrensRoom1":
            case "Kinderzimmer 1":
                resId = R.drawable.icon_new26;
                break;
            case "roomChildrensRoom2":
            case "Kinderzimmer 2":
                resId = R.drawable.icon_new26;
                break;
            case "roomOffice":
            case "Büro":
                resId = R.drawable.icon_new20;
                break;
            case "roomBathroom":
            case "Badezimmer":
                resId = R.drawable.icon9;
                break;
            case "roomGarage":
            case "Garage":
                resId = R.drawable.icon2;
                break;
            case "roomHWR":
            case "Hauswirtschaftsraum":
                resId = R.drawable.icon_new1;
                break;
            case "roomGarden":
            case "Garten":
                resId = R.drawable.icon3;
                break;
            case "roomTerrace":
            case "Terrasse":
                resId = R.drawable.icon_new34;
                break;
            case "funcLight":
            case "Licht":
                resId = R.drawable.icon11;
                break;
            case "funcHeating":
            case "Heizung":
                resId = R.drawable.icon17;
                break;
            case "funcClimateControl":
            case "Klima":
                resId = R.drawable.icon_new29;
                break;
            case "funcWeather":
            case "Wetter":
                resId = R.drawable.icon12;
                break;
            case "funcEnvironment":
            case "Umwelt":
                resId = R.drawable.icon3;
                break;
            case "funcSecurity":
            case "Sicherheit":
                resId = R.drawable.icon13;
                break;
            case "funcLock":
            case "Verschluss":
                resId = R.drawable.icon14;
                break;
            case "funcButton":
            case "Taster":
                resId = R.drawable.icon_new36;
                break;
            case "funcCentral":
            case "Zentrale":
                resId = R.drawable.icon15;
                break;
            case "funcEnergy":
            case "Energiemanagement":
                resId = R.drawable.icon16;
                break;
            default:
                String lowerCaseName = rawName.toLowerCase();

                if (lowerCaseName.contains("arbeit") || lowerCaseName.contains("work")) {
                    resId = R.drawable.icon_new40;
                } else if (lowerCaseName.contains("auto") || lowerCaseName.contains("car")) {
                    resId = R.drawable.icon2;
                } else if (lowerCaseName.contains("dach") || lowerCaseName.contains("attic")) {
                    resId = R.drawable.icon_new41;
                } else if (lowerCaseName.contains("einfahrt") || lowerCaseName.contains("driveway") || lowerCaseName.contains("straße") || lowerCaseName.contains("street")) {
                    resId = R.drawable.icon_new42;
                } else if (lowerCaseName.contains("wc ") || lowerCaseName.endsWith(" wc")) {
                    resId = R.drawable.icon4;
                } else if (lowerCaseName.contains("kind") || lowerCaseName.contains("child")) {
                    resId = R.drawable.icon_new26;
                }
                break;
        }

        if (resId != -1) {
            iv.setImageResource(resId);
            return true;
        }
        return false;
    }
}
