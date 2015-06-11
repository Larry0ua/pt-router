package dospring.controllers.model
import groovy.transform.Immutable
import groovy.transform.ToString
import model.Point
import model.Route

@Immutable
@ToString
class CalculatedRoute {
    List<RouteChunk> routeChunks

    static CalculatedRoute createRoute(List<RouteChunk> chunk) {
        List<RouteChunk> copy = new ArrayList<>(chunk)
        copy.removeAll {it.start - it.end < 1} // too close
        new CalculatedRoute(copy)
    }
    def CalculatedRoute append(RouteChunk chunk) {
        def copy = new ArrayList<>(this.routeChunks)
        copy.add(chunk)
        return new CalculatedRoute(copy)
    }
}

// for walk - routes = []
@Immutable(knownImmutableClasses = [Point])
@ToString
class RouteChunk {
    List<Route> routes
    Point start
    Point end
}
