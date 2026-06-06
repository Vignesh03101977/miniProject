package com.onetap.app.utils;

public class LocationUtils {

    private static final double EARTH_RADIUS = 6371000;

    public static boolean isInsideRectangle(
            double studentLat, double studentLng,
            double minLat, double maxLat,
            double minLng, double maxLng) {

        return studentLat >= minLat && studentLat <= maxLat
                && studentLng >= minLng && studentLng <= maxLng;
    }

    public static double[] calculateRectangleBoundary(
            double centerLat, double centerLng, double rangeMeters) {

        double latOffset = rangeMeters / 111000.0;
        double lngOffset = rangeMeters / (111000.0 * Math.cos(Math.toRadians(centerLat)));

        double minLat = centerLat - latOffset;
        double maxLat = centerLat + latOffset;
        double minLng = centerLng - lngOffset;
        double maxLng = centerLng + lngOffset;

        return new double[]{minLat, maxLat, minLng, maxLng};
    }

    public static double distanceBetween(
            double lat1, double lng1,
            double lat2, double lng2) {

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}