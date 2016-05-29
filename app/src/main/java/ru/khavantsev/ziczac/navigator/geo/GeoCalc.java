package ru.khavantsev.ziczac.navigator.geo;

import static java.lang.Math.*;

public class GeoCalc {

    private static final double RADIUS = 6372795;

    public static double orthodromeAzimuth(LatLon LatLon1, LatLon LatLon2) {
        double deltaLong = toRadians(LatLon2.longitude - LatLon1.longitude);

        double lat1 = toRadians(LatLon1.latitude);
        double lat2 = toRadians(LatLon2.latitude);

        double y = sin(deltaLong) * cos(lat2);
        double x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLong);
        double result = atan2(y, x);
        return result;
    }


    public static double orthodromeDistance(LatLon p1, LatLon p2) {
        double lat1 = Math.toRadians(p1.latitude);
        double lon1 = Math.toRadians(p1.longitude);
        double lat2 = Math.toRadians(p2.latitude);
        double lon2 = Math.toRadians(p2.longitude);

        if (lat1 == lat2 && lon1 == lon2)
            return 0;

        // "Haversine formula," taken from http://en.wikipedia.org/wiki/Great-circle_distance#Formul.C3.A6
        double a = Math.sin((lat2 - lat1) / 2.0);
        double b = Math.sin((lon2 - lon1) / 2.0);
        double c = a * a + +Math.cos(lat1) * Math.cos(lat2) * b * b;
        double distanceRadians = 2.0 * Math.asin(Math.sqrt(c));

        return Double.isNaN(distanceRadians) ? 0 : distanceRadians;
    }


    public static double rhumbDistance(LatLon p1, LatLon p2) {
        double lat1 = Math.toRadians(p1.latitude);
        double lon1 = Math.toRadians(p1.longitude);
        double lat2 = Math.toRadians(p2.latitude);
        double lon2 = Math.toRadians(p2.longitude);


        if (lat1 == lat2 && lon1 == lon2)
            return 0;

        // Taken from http://www.movable-type.co.uk/scripts/latlong.html
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double dPhi = Math.log(Math.tan(lat2 / 2.0 + Math.PI / 4.0) / Math.tan(lat1 / 2.0 + Math.PI / 4.0));
        double q = dLat / dPhi;
        if (Double.isNaN(dPhi) || Double.isNaN(q)) {
            q = Math.cos(lat1);
        }
        // If lonChange over 180 take shorter rhumb across 180 meridian.
        if (Math.abs(dLon) > Math.PI) {
            dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
        }

        double distanceRadians = Math.sqrt(dLat * dLat + q * q * dLon * dLon);

        return Double.isNaN(distanceRadians) ? 0 : distanceRadians;
    }

    public static double rhumbAzimuth(LatLon p1, LatLon p2) {

        double lat1 = Math.toRadians(p1.latitude);
        double lon1 = Math.toRadians(p1.longitude);
        double lat2 = Math.toRadians(p2.latitude);
        double lon2 = Math.toRadians(p2.longitude);

        if (lat1 == lat2 && lon1 == lon2)
            return 0;

        // Taken from http://www.movable-type.co.uk/scripts/latlong.html
        double dLon = lon2 - lon1;
        double dPhi = Math.log(Math.tan(lat2 / 2.0 + Math.PI / 4.0) / Math.tan(lat1 / 2.0 + Math.PI / 4.0));
        // If lonChange over 180 take shorter rhumb across 180 meridian.
        if (Math.abs(dLon) > Math.PI) {
            dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
        }
        double azimuthRadians = Math.atan2(dLon, dPhi);

        return Double.isNaN(azimuthRadians) ? 0 : azimuthRadians;
    }


    public static long toRealDistance(double radians) {
        return Math.round(radians * RADIUS);
    }

    public static double toRealAzimuth(double radians, float declination) {
        return ((Math.toDegrees(radians) - declination + 360.0) % 360.0);
    }

}
