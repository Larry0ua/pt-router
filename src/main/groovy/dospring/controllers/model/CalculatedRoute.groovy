package dospring.controllers.model

import groovy.transform.Immutable
import groovy.transform.ToString
import model.Route
import model.Stop

@Immutable
@ToString
class CalculatedRoute {
    List<RouteChunk> routeChunks

    static CalculatedRoute createRoute(RouteChunk chunk) {
        new CalculatedRoute([chunk])
    }
    def CalculatedRoute append(RouteChunk chunk) {
        def copy = new ArrayList<>(this.routeChunks)
        copy.add(chunk)
        return new CalculatedRoute(copy)
    }
}

// for walk - route = []
@Immutable(knownImmutableClasses = [Stop])
@ToString
class RouteChunk {
    List<Route> route
    Stop start
    Stop end
}
