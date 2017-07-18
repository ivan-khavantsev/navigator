package ru.khavantsev.ziczac.navigator.geo;

import static java.lang.Math.*;

public class GeoCalc {

    private static final double RADIUS = 6372795;

    public static double rhumbDistanceBetween(LatLon p1, LatLon p2) {

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

    public static double rhumbAzimuthBetween(LatLon p1, LatLon p2) {
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

    public static double distanceToRadians(long distance) {
        return (distance / RADIUS);
    }

    public static long toRealDistance(double radians) {
        return Math.round(radians * RADIUS);
    }

    public static double toRealAzimuth(double radians, float declination) {
        // TODO: Убрать это безобразие
        return ((Math.toDegrees(radians) - declination + 360.0) % 360.0);
    }

    /**
     * @param point    LatLon
     * @param distance double radians
     * @param angle    double radians
     * @return LatLon new point coordinates
     */
    public static LatLon projection(LatLon point, double distance, double angle) {
        double lat1 = Math.toRadians(point.latitude);
        double lon1 = Math.toRadians(point.longitude);

        double dLat = distance * cos(angle);
        double lat2 = lat1 + dLat;

        double dPhi = Math.log(Math.tan(lat2 / 2.0 + Math.PI / 4.0) / Math.tan(lat1 / 2.0 + Math.PI / 4.0));
        double q = dLat / dPhi;
        if (Double.isNaN(dPhi) || Double.isNaN(q)) {
            q = Math.cos(lat1);
        }

        double dLon = distance * sin(angle)/q;

        if (Math.abs(lat2) > Math.PI/2) lat2 = lat2>0 ? Math.PI-lat2 : -Math.PI-lat2;

        if (Math.abs(dLon) > Math.PI) {
            dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
        }

        double lon2 = lon1 + dLon;

        return new LatLon(Math.toDegrees(lat2), Math.toDegrees(lon2));
    }
}
