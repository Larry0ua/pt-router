package dospring.service
import dospring.controllers.model.CalculatedRoute
import dospring.controllers.model.RouteChunk
import dospring.storage.parser.OsmTransportStorage
import model.Route
import model.Stop
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RouteService {

    @Autowired
    OsmTransportStorage transportStorage

//    List<CalculatedRoute>

    List<CalculatedRoute> calculateRoutes(Collection<Stop> from, Collection<Stop> to) {
        List<CalculatedRoute> routes = []
        from?.each { f ->
            to?.each { t ->
                def rts = transportStorage.matrix.get(f)?.get(t)
                if (rts) {
                    routes << CalculatedRoute.createRoute(new RouteChunk(rts.toList().sort(), f, t))
                }
            }
        }

        return mergeRoutes(routes)
    }

    List<CalculatedRoute> complexCalculateRoutes(Collection<Stop> from, Collection<Stop> to) {

        // no route if nulls or empty lists are passed
        if (!from || !to) {
            return []
        }

        List<CalculatedRoute> routes = []
        List<Route> routesStart = transportStorage.routes.findAll {from.any{stop->it.platforms.contains(stop)}}
        List<Route> routesEnd =   transportStorage.routes.findAll {  to.any{stop->it.platforms.contains(stop)}}

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

        [from, intermediatePoints, to].combinations {Stop p1, Stop p2, Stop p3 ->
            Collection<Route> routes1 = routesStart.findAll { r -> r.isAfter(p1, p2) }
            Collection<Route> routes2 = routesEnd  .findAll { r -> r.isAfter(p2, p3) }
            if (routes1 && routes2) {
                routes << new CalculatedRoute([
                        new RouteChunk(routes1, p1, p2),
                        new RouteChunk(routes2, p2, p3)
                ])
            }
        }
        // find routes that differ in intermediate point only. Leave only one - where this point is farther from the start
        List<CalculatedRoute> routesToRemove = []
        for (int i = 0; i < routes.size() - 1; i++) {
            def r1 = routes[i]
            if (routesToRemove.contains(r1)) {
                continue
            }
            def similar = []
            for (int j = i + 1; j < routes.size(); j++) {
                def r2 = routes[j]
                if (r1.routeChunks[0].start == r2.routeChunks[0].start &&
                        intersecting(r1.routeChunks[0].route, r2.routeChunks[0].route) &&
                        intersecting(r1.routeChunks[1].route, r2.routeChunks[1].route) &&
                        r1.routeChunks[1].end == r2.routeChunks[1].end) {
                    similar << r2
                }
            }
            if (!similar.empty) {
                similar << r1
                def winner = similar.max {CalculatedRoute r -> r.routeChunks[0].route.size() * r.routeChunks[1].route.size()}
                similar -= winner
                routesToRemove += similar
            }
        }

        routes - routesToRemove
//        [routesStart, routesEnd].combinations {
//            Route r1, Route r2 ->
//                from.each { Stop f ->
//                    r1.eachAfter(f, { Stop intermediate ->
//                        to.each { Stop t ->
//                            if (r2.isAfter(intermediate, t))
//                                subresult << new RouteStopRoute(
//                                        start: f,
//                                        route1: r1,
//                                        intermediate: intermediate,
//                                        route2: r2,
//                                        end: t
//                                )
//                        }
//                    })
//                }
//        }
//
//        // remove 'duplicates' when only intermediate point differs.
//
//        subresult.collect { rsr ->
//            new CalculatedRoute([
//                    new RouteChunk([rsr.route1], rsr.start, rsr.intermediate),
//                    new RouteChunk([rsr.route2], rsr.intermediate, rsr.end),
//            ])
//        }
    }

    static boolean intersecting(List<Route> r1, List<Route> r2) {
        def intersects = r1.intersect(r2).size()
        if (intersects >= 2)
            return true
        if (intersects == 1 && (r1.size() == 1 || r2.size() == 1)) {
            return true
        }
        false
    }

    List<CalculatedRoute> mergeRoutes(List<CalculatedRoute> original) {
        // each call verifies only the last chunks in the original routes

        // 1. remove duplicates
        original.toSet().toList()
    }
}
