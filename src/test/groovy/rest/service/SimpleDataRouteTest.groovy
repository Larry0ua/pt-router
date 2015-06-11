package rest.service
import dospring.controllers.model.CalculatedRoute
import dospring.processor.matrix.MatrixProcess
import dospring.service.RouteService
import dospring.service.RouteSimplifierService
import dospring.service.StopService
import dospring.storage.parser.TransportDataProvider
import dospring.storage.parser.TransportStorage
import model.Point
import model.Route
import model.Stop
import org.junit.Before
import org.junit.Test

class SimpleDataRouteTest {

    Point point0 = new Point(lat:47.9, lon:24.0001) // stop 0
    Point point1 = new Point(lat:48.0, lon:24.0001) // stop 1
    Point point2 = new Point(lat:48.1, lon:24.0001) // stop 2
    Point point3 = new Point(lat:48.2, lon:24.0001) // stop 3
    Point point4 = new Point(lat:48.3, lon:24.0001) // stop 4
    Point point5 = new Point(lat:48.4, lon:24.0001) // stop 5
    Point point6 = new Point(lat:48.5, lon:24.0001) // stop 6
    Point point7 = new Point(lat:48.5, lon:24.1001) // stop 7
    Point point8 = new Point(lat:48.5, lon:24.2001) // stop 8

    Stop stop0 = new Stop(name: "stop 0", lat: 47.9, lon: 24)
    Stop stop1 = new Stop(name: "stop 1", lat: 48.0, lon: 24)
    Stop stop2 = new Stop(name: "stop 2", lat: 48.1, lon: 24)
    Stop stop3 = new Stop(name: "stop 3", lat: 48.2, lon: 24)
    Stop stop4 = new Stop(name: "stop 4", lat: 48.3, lon: 24)
    Stop stop5 = new Stop(name: "stop 5", lat: 48.4, lon: 24)
    Stop stop6 = new Stop(name: "stop 6", lat: 48.5, lon: 24)
    Stop stop7 = new Stop(name: "stop 7", lat: 48.5, lon: 24.1)
    Stop stop8 = new Stop(name: "stop 8", lat: 48.5, lon: 24.2)

    Stop stop10 = new Stop(name: "stop 10", lat: 49.0,   lon: 24.0 ) // 500m is around 0.006 in this location
    Stop stop11 = new Stop(name: "stop 11", lat: 49.0,   lon: 24.01)
    Stop stop12 = new Stop(name: "stop 12", lat: 49.0,   lon: 24.012)
    Stop stop13 = new Stop(name: "stop 13", lat: 49.0,   lon: 24.02)
    Stop stop14 = new Stop(name: "stop 14", lat: 49.0,   lon: 24.03)
    Stop stop15 = new Stop(name: "stop 15", lat: 49.0,   lon: 24.04)
    Stop stop16 = new Stop(name: "stop 16", lat: 49.0,   lon: 24.042)
    Stop stop17 = new Stop(name: "stop 17", lat: 49.0,   lon: 24.05)
    Stop stop18 = new Stop(name: "stop 18", lat: 49.001, lon: 24.03)
    Stop stop19 = new Stop(name: "stop 19", lat: 49.01,  lon: 24.03)

    @Test
    void "test stop is found"() {
        def stop = stopService.findNearestStop(point1)
        assert "stop 1" == stop.name

        def stop2 = stopService.findNearestStop(point2)
        assert null != stop2
        assert "stop 2" == stop2.name
    }

    @Test
    void "test route between two stops is found"() {
        List<CalculatedRoute> routes = routeService.findSimpleRoute(point1, point2)

        assert [[[], ["R1", "R2"], []]] == routes.routeChunks.route.name
    }

    @Test
    void "test longer route between two stops is found"() {
        List<CalculatedRoute> routes = routeService.findSimpleRoute(point1, point4)

        assert [[[], ["R2"], []]] == routes.routeChunks.route.name
    }

    @Test
    void "test route with one route switch"() {
        List<CalculatedRoute> routes = routeService.findRouteWithOneSwitchWithGaps(point2, point5)

        assert [[[], ["R2"], ["R3"], []]] == routes.routeChunks.route.name
    }

    @Test
    void "test longer route with one route swtich"() {
        List<CalculatedRoute> routes = routeService.findRouteWithOneSwitchWithGaps(point1, point8)

        // by 2, then by 5 - should be eliminated
        // by 2, then by 4 or 5
        assert [[[], ["R2"], ["R4", "R5"], []]] == routes.routeChunks.route.name
    }

    @Test
    void "test that path is not found with zero or one swtich"() {
        List<CalculatedRoute> routes = routeService.findSimpleRoute(point6, point8)

        assert routes.empty

        routes = routeService.findRouteWithOneSwitchWithGaps(point6, point8)

        assert routes.empty

    }

    @Test
    void "test that direct path is not found"() {
        List<CalculatedRoute> routes = routeService.findSimpleRoute(point2, point5)

        assert routes.empty
    }

    @Test
    void "test routes are not duplicated when there are more than one possible point to do a swtich"() {
        List<CalculatedRoute> routes = routeService.findRouteWithOneSwitchWithGaps(point0, point3)

        assert [[[], ["R1"], ["R2"], []]] == routes.routeChunks.route.name
    }

    @Test
    void "test route with two switches"() {
        List<CalculatedRoute> routes = routeService.findRouteWithTwoSwitchesAndGaps(point0, point8)

        assert [[[], ["R1"], ["R2"], ["R4", "R5"], []]] == routes.routeChunks.route.name
    }


    @Test
    void "test route with two switches and one gap"() {
        List<CalculatedRoute> routes = routeService.findRouteWithTwoSwitchesAndGaps(stop10, stop15)

        assert [[["R6"], [], ["R7"], ["R8"]]] == routes.routeChunks.route.name
    }


    @Test
    void "test route with two switches and two gaps"() {
        List<CalculatedRoute> routes = routeService.findRouteWithTwoSwitchesAndGaps(stop19, stop17)

        assert [[["R10"], [], ["R8"], [], ["R9"]]] == routes.routeChunks.route.name
    }

    @Test
    void "test that 1-switch version does not out no-switch paths"() {
        List<CalculatedRoute> routes = routeService.findRouteWithOneSwitchWithGaps(point0, point2)

        assert routes.empty
    }

    @Test
    void "test path with 1 switch with a gap"() {
        List<CalculatedRoute> routes = routeService.findRouteWithOneSwitchWithGaps(stop10, stop13)

        assert [[["R6"], [], ["R7"]]] == routes.routeChunks.route.name
    }

    @Test
    void "test path with 1 switch with gap overlapping 2nd route"() {
        List<CalculatedRoute> routes = routeService.findRouteWithOneSwitchWithGaps(stop12, stop15)

        assert [[["R7"], ["R8"]]] == routes.routeChunks.route.name
    }

    TransportStorage storage
    RouteService routeService
    StopService stopService

    // TODO: think of the test case where we may eliminate similarity only at the end of the route for the 2-switch version

    @Before
    void mockTransportProvider() {
        if (storage == null) {
            TransportStorage initialized = new TransportStorage(
                    matrixProcess: new MatrixProcess(),
                    maxDistance: 2000,
                    filename: "transport_ch.osm", // not used - all data is from provider below
                    transportDataProvider: new TransportDataProvider() {
                        @Override
                        def parseFile(String filename) {


                            setStops([stop0, stop1, stop2, stop3, stop4, stop5, stop6, stop7, stop8,
                                    stop10, stop11, stop12, stop13, stop14, stop15, stop16, stop17, stop18, stop19
                            ])
                            setRoutes([
                                    new Route(name: "R1", id:1, type: "bus", platforms: [stop0, stop1, stop2]),
                                    new Route(name: "R2", id:2, type: "bus", platforms: [stop1, stop2, stop3, stop4]),
                                    new Route(name: "R3", id:3, type: "bus", platforms: [stop3, stop4, stop5, stop6]),
                                    new Route(name: "R4", id:4, type: "bus", platforms: [stop4, stop7, stop8]),
                                    new Route(name: "R5", id:5, type: "bus", platforms: [stop3, stop4, stop7, stop8]),
                                    new Route(name: "R6", id:6, type: "bus", platforms: [stop10, stop11]),
                                    new Route(name: "R7", id:7, type: "bus", platforms: [stop12, stop13]),
                                    new Route(name: "R8", id:8, type: "bus", platforms: [stop13, stop14, stop15]),
                                    new Route(name: "R9", id:9, type: "bus", platforms: [stop16, stop17]),
                                    new Route(name: "R10",id:10,type: "bus", platforms: [stop19, stop18]),
                            ])
                            /*
                            0(1) -> 1(1,2) -> 2(1,2) -> 3(2,3,5) -> 4(2,3,4,5) -> 5(3) -> 6(3)
                                                                    |
                                                                    \-> 7(4,5) -> 8(4,5)

                            10 (6) -> 11 (6) .. 12 (7) -> 13 (7,8) .. 14 (8) -> 15 (8) .. 16 (9) -> 17(9)
                                                                       ..
                                                                      18 (10)
                                                                      19 (10)
                             */
                        }
                    }
            )
            initialized.init()
            storage = initialized

            stopService = new StopService(maxDistance: 500, transportStorage: storage)
            routeService = new RouteService(
                    transportStorage: storage,
                    stopService: stopService,
                    routeSimplifierService: new RouteSimplifierService()
            )

        }
    }
}
