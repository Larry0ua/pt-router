package dospring.storage.parser

import model.Point
import model.Stop

class StopsStorage {
    Map<Integer, Map<Integer, List<Stop>>> storage = [:].withDefault {[:].withDefault {[]}}

    void putAll(Collection<Stop> stops) {
        stops.each {
            storage[mapx(it)][mapy(it)] << it
        }
    }

    Collection<Stop> getNear(Point p) {
        def x = mapx(p)
        def y = mapy(p)
        def result = []
        result.addAll(storage[x][y])
        result.addAll(storage[x-1][y-1])
        result.addAll(storage[x-1][y])
        result.addAll(storage[x-1][y+1])
        result.addAll(storage[x][y-1])
        result.addAll(storage[x][y+1])
        result.addAll(storage[x+1][y-1])
        result.addAll(storage[x+1][y])
        result.addAll(storage[x+1][y+1])
        result
    }

    static int mapx(Point p) {
        (int)(p.lat * 30)
    }
    static int mapy(Point p) {
        (int)(p.lon * 30)
    }
}
