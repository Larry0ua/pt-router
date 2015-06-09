package rest.service
import dospring.controllers.model.CalculatedRoute
import dospring.processor.matrix.MatrixProcess
import dospring.service.RouteService
import dospring.service.StopService
import dospring.storage.parser.TransportStorage
import dospring.storage.parser.TransportDataProvider
import drawing.RouteDrawer
import model.Point
import model.Route
import model.Stop
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class ServicesTest {

    Point point0 = new Point(lat:47.9, lon:24.0001) // stop 0
    Point point1 = new Point(lat:48.0, lon:24.0001) // stop 1
    Point point2 = new Point(lat:48.1, lon:24.0001) // stop 2
    Point point3 = new Point(lat:48.2, lon:24.0001) // stop 3
    Point point4 = new Point(lat:48.3, lon:24.0001) // stop 4
    Point point5 = new Point(lat:48.4, lon:24.0001) // stop 5
    Point point6 = new Point(lat:48.5, lon:24.0001) // stop 6
    Point point7 = new Point(lat:48.5, lon:24.1001) // stop 7
    Point point8 = new Point(lat:48.5, lon:24.2001) // stop 8

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
        def stops1 = stopService.findNearestStops(point1)
        def stops2 = stopService.findNearestStops(point2)

        List<CalculatedRoute> routes = routeService.findSimpleRoute(stops1, stops2)

        assert routes.routeChunks.every{it.size() == 1}
        assert [[["R1", "R2"]]] == routes.routeChunks.route.name
    }

    @Test
    void "test longer route between two stops is found"() {
        def stops1 = stopService.findNearestStops(point1)
        def stops4 = stopService.findNearestStops(point4)

        List<CalculatedRoute> routes = routeService.findSimpleRoute(stops1, stops4)

        assert [[["R2"]]] == routes.routeChunks.route.name
    }

    @Test
    void "test route with one route switch"() {
        def stop2 = stopService.findNearestStops(point2)
        def stop5 = stopService.findNearestStops(point5)

        List<CalculatedRoute> routes = routeService.findRouteWithOneSwitch(stop2, stop5)

        assert [[["R2"], ["R3"]]] == routes.routeChunks.route.name
    }

    @Test
    void "test longer route with one route swtich"() {
        def stop1 = stopService.findNearestStops(point1)
        def stop8 = stopService.findNearestStops(point8)

        List<CalculatedRoute> routes = routeService.findRouteWithOneSwitch(stop1, stop8)

        // by 2, then by 5 - should be eliminated
        // by 2, then by 4 or 5
        assert [[["R2"], ["R4", "R5"]]] == routes.routeChunks.route.name
    }

    @Test
    void "test that path is not found with zero or one swtich"() {
        def stop6 = stopService.findNearestStops(point6)
        def stop8 = stopService.findNearestStops(point8)

        List<CalculatedRoute> routes = routeService.findSimpleRoute(stop6, stop8)

        assert routes.empty

        routes = routeService.findRouteWithOneSwitch(stop6, stop8)

        assert routes.empty

    }

    @Test
    void "test that direct path is not found"() {
        def stop2 = stopService.findNearestStops(point2)
        def stop5 = stopService.findNearestStops(point5)

        List<CalculatedRoute> routes = routeService.findSimpleRoute(stop2, stop5)

        assert routes.empty
    }

    @Test
    void "test routes are not duplicated when there are more than one possible point to do a swtich"() {
        def stops0 = stopService.findNearestStops(point0)
        def stops3 = stopService.findNearestStops(point3)

        List<CalculatedRoute> routes = routeService.findRouteWithOneSwitch(stops0, stops3)

        assert [[["R1"], ["R2"]]] == routes.routeChunks.route.name
    }

    @Test
    void "test route with two switches"() {
        def stops0 = stopService.findNearestStops(point0)
        def stops8 = stopService.findNearestStops(point8)

        List<CalculatedRoute> routes = routeService.findRouteWithTwoSwitches(stops0, stops8)

        assert [[["R1"], ["R2"], ["R4", "R5"]]] == routes.routeChunks.route.name
    }

    @Test
    @Ignore
    void drawRoutes() {
        def drawer = new RouteDrawer()

        storage.routes.each {drawer.drawRoute(it, "${it.id}.png")}
    }

    TransportStorage storage
    RouteService routeService
    StopService stopService

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
                            Stop s0 = new Stop(name: "stop 0", lat: 47.9, lon: 24)
                            Stop s1 = new Stop(name: "stop 1", lat: 48.0, lon: 24)
                            Stop s2 = new Stop(name: "stop 2", lat: 48.1, lon: 24)
                            Stop s3 = new Stop(name: "stop 3", lat: 48.2, lon: 24)
                            Stop s4 = new Stop(name: "stop 4", lat: 48.3, lon: 24)
                            Stop s5 = new Stop(name: "stop 5", lat: 48.4, lon: 24)
                            Stop s6 = new Stop(name: "stop 6", lat: 48.5, lon: 24)
                            Stop s7 = new Stop(name: "stop 7", lat: 48.5, lon: 24.1)
                            Stop s8 = new Stop(name: "stop 8", lat: 48.5, lon: 24.2)

                            setStops([s0, s1, s2, s3, s4, s5, s6, s7, s8])
                            setRoutes([
                                    new Route(name: "R1", id:1, type: "bus", platforms: [s0, s1, s2]),
                                    new Route(name: "R2", id:2, type: "bus", platforms: [s1, s2, s3, s4]),
                                    new Route(name: "R3", id:3, type: "bus", platforms: [s3, s4, s5, s6]),
                                    new Route(name: "R4", id:4, type: "bus", platforms: [s4, s7, s8]),
                                    new Route(name: "R5", id:5, type: "bus", platforms: [s3, s4, s7, s8]),
                            ])
                            /*
                            0(1) -> 1(1,2) -> 2(1,2) -> 3(2,3,5) -> 4(2,3,4,5) -> 5(3) -> 6(3)
                                                                    |
                                                                    \-> 7(4,5) -> 8(4,5)
                             */
                        }
                    }
            )
            initialized.init()
            storage = initialized

            routeService = new RouteService(transportStorage: storage)
            stopService = new StopService(maxDistance: 500, transportStorage: storage)

        }
    }
}
