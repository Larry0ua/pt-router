package dospring.service
import dospring.controllers.model.CalculatedRoute
import dospring.controllers.model.RouteChunk
import dospring.storage.parser.TransportStorage
import model.Route
import model.Stop
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RouteService {

    @Autowired
    TransportStorage transportStorage

//    List<CalculatedRoute>

    List<CalculatedRoute> findSimpleRoute(Collection<Stop> from, Collection<Stop> to) {
        List<CalculatedRoute> routes = []
        from?.each { f ->
            to?.each { t ->
                def rts = transportStorage.matrix.get(f)?.get(t)
                if (rts) {
                    routes << CalculatedRoute.createRoute(new RouteChunk(rts.toList().sort(), f, t))
                }
            }
        }

        routes
    }

    List<CalculatedRoute> findRouteWithOneSwitch(Collection<Stop> from, Collection<Stop> to) {

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
            if (routes1.intersect(routes2)) {
                return // we already have found this route in no-switch version
            }
            if (routes1 && routes2) {
                routes << new CalculatedRoute([
                        new RouteChunk(routes1, p1, p2),
                        new RouteChunk(routes2, p2, p3)
                ])
            }
        }

        dropSimilarRoutes(routes, 0)
    }

    // find routes that differ in intermediate point only. Leave only one - where this point is farther from the start
    private static List<CalculatedRoute> dropSimilarRoutes(List<CalculatedRoute> routes, int level) {
        if (level < 0) {
            return routes
        }
        List<CalculatedRoute> routesToRemove = []
        for (int i = 0; i < routes.size() - 1; i++) {
            def r1 = routes[i]
            if (routesToRemove.contains(r1)) {
                continue
            }
            def similar = []
            for (int j = i + 1; j < routes.size(); j++) {
                def r2 = routes[j]
                if (r1.routeChunks.size() < level + 1 && r2.routeChunks.size() < level + 1 &&
                        routesSimilar(r1.routeChunks[level  ].route, r2.routeChunks[level  ].route) &&
                        routesSimilar(r1.routeChunks[level+1].route, r2.routeChunks[level+1].route)) {

                    similar << r2
                }
            }
            if (!similar.empty) {
                similar << r1
                def winner = similar.max { CalculatedRoute r -> r.routeChunks[level].route.size() * r.routeChunks[level+1].route.size() }
                similar -= winner
                routesToRemove += similar
            }
        }
        routes - routesToRemove
    }

    static boolean routesSimilar(List<Route> r1, List<Route> r2) {
        def intersects = r1.intersect(r2).size()
        if (intersects >= 2)
            return true
        if (intersects == 1 && (r1.size() == 1 || r2.size() == 1)) {
            return true
        }
        false
    }

    List<CalculatedRoute> findRouteWithTwoSwitches(List<Stop> from, List<Stop> to) {
        // logic is similar to 1-switch version, but merging similar routes is more complex

        // no route if nulls or empty lists are passed
        if (!from || !to) {
            return []
        }

        List<CalculatedRoute> routes = []
        List<Route> routesStart = transportStorage.routes.findAll {from.any{stop->it.platforms.contains(stop)}}
        List<Route> routesEnd   = transportStorage.routes.findAll {  to.any{stop->it.platforms.contains(stop)}}
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

        [from, intermediateFrom, intermediateTo, to].combinations {Stop p1, Stop p2, Stop p3, Stop p4 ->
            Collection<Route> routes1 = routesStart.findAll { r -> r.isAfter(p1, p2) }
            Collection<Route> routes2 = routesAll  .findAll { r -> r.isAfter(p2, p3) }
            Collection<Route> routes3 = routesEnd  .findAll { r -> r.isAfter(p3, p4) }
            if (routes1 && routes2 && routes3) {
                routes << new CalculatedRoute([
                        new RouteChunk(routes1, p1, p2),
                        new RouteChunk(routes2, p2, p3),
                        new RouteChunk(routes3, p3, p4)
                ])
            }
        }

        // two optimizations should be here:
        // 1. eliminate routes with same route names but different stops - look for stops with the same condition as in 1-switch version
        // 2. remove subsets - 1, 2, (4,5) should have priority over 1, 2, 5 or 1, 2, 4, stops do not matter this time
        routes = dropSimilarRoutes(routes, 0)
        routes = dropSimilarRoutes(routes, 1)
        routes
    }
}
