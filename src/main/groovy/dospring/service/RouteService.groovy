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

    @Autowired
    RouteSimplifierService routeSimplifierService

    List<CalculatedRoute> findSimpleRoute(Point from, Point to) {
        List<CalculatedRoute> routes = []

        def stopsFrom = stopService.findNearestStops(from)
        def stopsTo = stopService.findNearestStops(to)

        def routesFrom = transportStorage.routes.findAll {stopsFrom.any{stop->it.contains(stop)}}

        stopsFrom?.each { f ->
            stopsTo?.each { t ->
                def rts = routesFrom.findAll {it.isAfter(f, t)}
                if (rts) {
                    routes << CalculatedRoute.createRoute([
                            new RouteChunk([], from, f),
                            new RouteChunk(rts.toList(), f, t),
                            new RouteChunk([], t, to)
                    ])
                }
            }
        }
        routeSimplifierService.dropSimilarRoutes(routes)
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

        from.each { Stop p1 ->
            intermediatePoints.each { Stop p2 ->
                // intermediate points are tied to start points by routes. Find routes1, then find nearest stops to these intermediate and check routes2 from these nearest stops
                def routes1 = routesStart.findAll { r -> r.isAfter(p1, p2) }

                if (routes1) {
                    Collection<Stop> nearIntermediate = stopService.findNearestStops(p2)
                    nearIntermediate.each { Stop p2walk ->

                        to.each { Stop p3 ->

                            // should not print routes in which we would walk and use same route as was on the previous stop
                            def routes2 = routesEnd.findAll { r -> r.isAfter(p2walk, p3) }

                            if (routes2
                                    && !routes1.intersect(routes2)  // TODO: think about replacing with routes2.containsAll(routes1)
                                    && (p2 == p2walk || !routes2.every { r -> r.contains(p2) })) {
                                // no need to walk if this gap is covered by all routes
                                routes << CalculatedRoute.createRoute([
                                        new RouteChunk([], fromPoint, p1),
                                        new RouteChunk(routes1, p1, p2),
                                        new RouteChunk([], p2, p2walk),
                                        new RouteChunk(routes2, p2walk, p3),
                                        new RouteChunk([], p3, toPoint)
                                ])
                            }
                        }
                    }
                }
            }
        }

        routeSimplifierService.dropSimilarRoutes(routes)
    }

    // find routes that differ in intermediate point only. Leave only one - where this point is farther from the start
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

                    routes << CalculatedRoute.createRoute([
                            new RouteChunk([], fromPoint, p1),
                            new RouteChunk(routes1, p1, p2),
                            new RouteChunk([], p2, p2walk),
                            new RouteChunk(routesInterm, p2walk, p3walk),
                            new RouteChunk([], p3walk, p3),
                            new RouteChunk(routes2, p3, p4),
                            new RouteChunk([], p4, toPoint)
                    ])
                }
            }
        }

        routeSimplifierService.dropSimilarRoutes(routes)
    }


}
