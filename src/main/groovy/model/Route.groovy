package model

import groovy.transform.EqualsAndHashCode
import groovy.transform.Immutable
import groovy.transform.Sortable
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
@Immutable(knownImmutableClasses = [Stop]) // we instantiate Stops only once while context creation
@Sortable(excludes = "platforms")
class Route {
    String type
    String ref
    String name
    List<Stop> platforms;
    String id

    boolean isAfter(Stop stop1, Stop stop2) {
        int idx1 = platforms.indexOf(stop1)
        int idx2 = platforms.indexOf(stop2)
        if (idx1 >= 0 && idx2 >= 0) {
            return idx2 > idx1
        }
        false
    }
    def eachAfter(Stop stop, Closure<?> closure) {
        int idx = platforms.indexOf(stop)
        if (idx >= 0) {
            ((idx + 1)..<platforms.size()).each { Integer it ->
                closure.call(platforms[it])
            }
        }
    }

    def eachBefore(Stop stop, Closure<?> closure) {
        int idx = platforms.indexOf(stop)
        if (idx > 0) {
            (0..<idx).each {
                closure.call(platforms[it])
            }
        }

    }
}
