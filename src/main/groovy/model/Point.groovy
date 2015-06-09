package model

import groovy.transform.ToString

@ToString
class Point {

    double lat
    double lon

    static Point valueOf(String value) {
        def split = value.split(",")
        if (split.length == 2) {
            return new Point(lat: split[0].toDouble(),
                             lon: split[1].toDouble())
        }
        null
    }

    double minus(Point stop) {
        Math.sqrt((lat - stop.lat) ** 2 + (lon - stop.lon) ** 2) * Math.PI * 6.4e6 / 180.0;
    }
}
