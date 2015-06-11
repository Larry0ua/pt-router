package routing

import dospring.processor.matrix.MatrixProcess
import dospring.service.RouteService
import dospring.service.RouteSimplifierService
import dospring.service.StopService
import dospring.storage.parser.TransportDataProvider
import dospring.storage.parser.TransportStorage
import model.Route
import model.Stop
import org.junit.Before
import org.junit.Test

class SemiRealRouteTest {

    TransportStorage storage
    StopService stopService
    RouteService routeService

    Map<String, Stop> s = [
            s1 : new Stop(lat: 48.25115, lon: 25.95451, name: 's1'),
            s2 : new Stop(lat: 48.294896, lon: 25.9370535, name : 'shkilna'),
            s3 : new Stop(lat: 48.2974085, lon: 25.9368497, name: 'barbusa'),
            s4 : new Stop(lat: 48.3103674, lon: 25.9182448, name: 'prut'),
            s5 : new Stop(lat: 48.3213803, lon: 25.9038441, name: 'metro'),
            s12 : new Stop(lat: 48.2946462, lon: 25.9365815, name: 'teatr'),
            s6 : new Stop(lat: 48.2818638, lon: 25.9357941, name: 'sadova'),
            s7 : new Stop(lat: 48.3190936, lon: 25.939048, name: 'epicentr'),
            s8 : new Stop(lat: 48.3344068, lon: 25.9468151, name: 'naftobaza'),
            s9 : new Stop(lat: 48.3724241, lon: 25.9997122, name: 'chornivka')
    ]

    List<Route> r = [
            new Route(name: "R1a", ref: "1a", type: "bus", platforms: [s.s1, s.s2, s.s3, s.s4, s.s5]),
            new Route(name: "R3a", ref: "3a", type: "trolleybus", platforms: [s.s1, s.s12, s.s3, s.s4]),
            new Route(name: "R36", ref: "36", type: "bus", platforms: [s.s6, s.s12, s.s3, s.s4, s.s7, s.s9]),
            new Route(name: "R37", ref: "37", type: "bus", platforms: [s.s6, s.s12, s.s3, s.s4, s.s8, s.s9])
    ]

    /*
        s1 -> s2 -> s3 -> s4 -> s5
            \s12 /
     */

    @Test
    void "test switch is at the farthest possible point"() {
        def route = routeService.findRouteWithOneSwitchWithGaps(s.s1, s.s9)
        assert [[["1a", "3a"], ["36", "37"]]] == route.routeChunks.routes.ref
    }

    @Before
    void setupTransport() {
        TransportStorage initialized = new TransportStorage(
                matrixProcess: new MatrixProcess(),
                maxDistance: 2000,
                filename: "transport_ch.osm", // not used - all data is from provider below
                transportDataProvider: new TransportDataProvider() {
                    @Override
                    def parseFile(String filename) {


                        setStops(s.values())
                        setRoutes(r)
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
