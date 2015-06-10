package dospring.service
import dospring.controllers.model.CalculatedRoute
import dospring.controllers.model.RouteChunk
import dospring.storage.parser.TransportStorage
import model.Point
import model.Route
import model.Stop
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RouteService {

    @Autowired
    TransportStorage transportStorage

    @Autowired
    StopService stopService

    List<CalculatedRoute> findSimpleRoute(Point from, Point to) {
        List<CalculatedRoute> routes = []

        def stopsFrom = stopService.findNearestStops(from)
        def stopsTo = stopService.findNearestStops(to)

        stopsFrom?.each { f ->
            stopsTo?.each { t ->
                def rts = transportStorage.matrix.get(f)?.get(t)
                if (rts) {
                    routes << CalculatedRoute.createRoute(new RouteChunk(rts.toList().sort(), f, t))
                }
            }
        }
        dropSimilarRoutes(routes, from, to)
    }

    List<CalculatedRoute> findRouteWithOneSwitchWithGaps(Point fromPoint, Point toPoint) {

        Collection<Stop> from = stopService.findNearestStops(fromPoint)
        Collection<Stop> to = stopService.findNearestStops(toPoint)

        // no route if no stops are found around given points
        if (!from || !to) {
            return []
        }

        List<CalculatedRoute> routes = []
        List<Route> routesStart = transportStorage.routes.findAll {from.any{stop->it.contains(stop)}}
        List<Route> routesEnd =   transportStorage.routes.findAll {  to.any{stop->it.contains(stop)}}

        // look for intermediate stops
        Set<Stop> intermediatePoints = []
        from.each { Stop f ->
            routesStart.each {
                it.eachAfter(f, {
                    intermediatePoints << it
                })
            }
        }
        // if there is no intermediate points between start and end
        if (intermediatePoints.empty) {
            return []
        }

        [from, intermediatePoints, to].eachCombination { Stop p1, Stop p2, Stop p3 ->
            // intermediate points are tied to start points by routes. Find routes1, then find nearest stops to these intermediate and check routes2 from these nearest stops
            def routes1 = routesStart.findAll { r -> r.isAfter(p1, p2) }

            Collection<Stop> nearIntermediate = stopService.findNearestStops(p2)
            nearIntermediate.each { Stop p2walk ->

                // should not print routes in which we would walk and use same route as was on the previous stop
                def routes2 = routesEnd.findAll { r -> r.isAfter(p2walk, p3) }

                if (routes1 && routes2
                        && !routes1.intersect(routes2)  // TODO: think about replacing with routes2.containsAll(routes1)
                        && (!routes2.every {r -> r.contains(p2)} || p2 == p2walk)) { // no need to walk if this gap is covered by all routes
                    def routeChunks = []
                    routeChunks << new RouteChunk(routes1, p1, p2)
                    if (p2 != p2walk)
                        routeChunks << new RouteChunk([], p2, p2walk)
                    routeChunks << new RouteChunk(routes2, p2walk, p3)
                    routes << new CalculatedRoute(routeChunks)
                }
            }
        }

        dropSimilarRoutes(routes, fromPoint, toPoint)
    }

    // find routes that differ in intermediate point only. Leave only one - where this point is farther from the start
    private static List<CalculatedRoute> dropSimilarRoutes(List<CalculatedRoute> routes, Point start, Point end) {
        List<CalculatedRoute> routesToRemove = []
        List<CalculatedRoute> processed = []
        for (int i = 0; i < routes.size() - 1; i++) {
            def r1 = routes[i]
            if (processed.contains(r1)) {
                continue
            }
            def similar = []
            for (int j = i + 1; j < routes.size(); j++) {
                def r2 = routes[j]
                if (r1.routeChunks.size() != r2.routeChunks.size()) {
                    continue
                }
                if ((0..<(r1.routeChunks.size())).every {Integer idx ->
                    routesSimilar(r1.routeChunks[idx].route, r2.routeChunks[idx].route)}) {

                    similar << r2
                }
            }
            if (!similar.empty) {
                similar << r1
                r1.routeChunks.collect{it.route.size()}.inject(1, {a,b->a*b})
                processed += similar
                // max by multiplication of the |routes|
                def winner = similar.max { CalculatedRoute r ->
                    1000 * r.routeChunks.collect{it.route.size()}.inject(0, {a,b->a*b})
                    + (r.routeChunks.first().start - start) // start distance in meters
                    + (r.routeChunks.last() .end   - end) // plus end distance in meters
                }
                routesToRemove += similar - winner
            }
        }
        routes - routesToRemove
    }

    private static boolean routesSimilar(List<Route> r1, List<Route> r2) {
        if (!r1 && !r2) {
            return true
        }
        def intersects = r1.intersect(r2).size()
        if (intersects >= 2)
            return true
        if (intersects == 1 && (r1.size() == 1 || r2.size() == 1)) {
            return true
        }
        false
    }

    List<CalculatedRoute> findRouteWithTwoSwitchesAndGaps(Point fromPoint, Point toPoint) {

        Collection<Stop> from = stopService.findNearestStops(fromPoint)
        Collection<Stop> to = stopService.findNearestStops(toPoint)

        // no route if no stops are found around given points
        if (!from || !to) {
            return []
        }

        List<CalculatedRoute> routes = []
        List<Route> routesStart = transportStorage.routes.findAll {from.any{stop->it.contains(stop)}}
        List<Route> routesEnd =   transportStorage.routes.findAll {  to.any{stop->it.contains(stop)}}
        List<Route> routesAll   = transportStorage.routes

        // look for intermediate stops
        Set<Stop> intermediateFrom = []
        from.each { Stop f ->
            routesStart.each {
                it.eachAfter(f, {
                    intermediateFrom << it
                })
            }
        }
        Set<Stop> intermediateTo = []
        to.each { Stop t ->
            routesEnd.each {
                it.eachBefore(t, {
                    intermediateTo << it
                })
            }
        }

        if (!intermediateFrom || !intermediateTo) {
            return []
        }

        [from, intermediateFrom, intermediateTo, to].eachCombination { Stop p1, Stop p2, Stop p3, Stop p4 ->
            // from p1 to p2
            // from p2 to p3
            // from near p2 to near p3 - combinations
            // from p3 to p4
            def routes1 = routesStart.findAll { r -> r.isAfter(p1, p2) }
            def routes2 = routesEnd.findAll   { r -> r.isAfter(p3, p4) }
            def nearIntermediate1 = stopService.findNearestStops(p2)
            def nearIntermediate2 = stopService.findNearestStops(p3)
            if (!routes1 || !routes2) {
                return
            }
            [nearIntermediate1, nearIntermediate2].eachCombination { Stop p2walk, Stop p3walk ->
                def routesInterm = routesAll.findAll {r -> r.isAfter(p2walk, p3walk) }

                if ( routesInterm
                        && !(routes1.intersect(routesInterm) && routes2.intersect(routesInterm) && routes1.intersect(routes2))
                        && (!routesInterm.every {it.contains(p2)} || p2 == p2walk)
                        && (!routesInterm.every {it.contains(p3)} || p3 == p3walk)
                ) {
                    def routeChunks = []
                    routeChunks << new RouteChunk(routes1, p1, p2)
                    if (p2 != p2walk)
                        routeChunks << new RouteChunk([], p2, p2walk)
                    routeChunks << new RouteChunk(routesInterm, p2walk, p3walk)
                    if (p3 != p3walk)
                        routeChunks << new RouteChunk([], p3walk, p3)
                    routeChunks << new RouteChunk(routes2, p3, p4)

                    routes << new CalculatedRoute(routeChunks)
                }
            }
        }

        dropSimilarRoutes(routes, fromPoint, toPoint)
    }
}
