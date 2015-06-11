package rest.service

import dospring.processor.matrix.MatrixProcess
import dospring.service.RouteService
import dospring.service.StopService
import dospring.storage.parser.TransportDataProvider
import dospring.storage.parser.TransportStorage
import model.Point
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

class RealDataRouteTest {

    static StopService stopService
    static RouteService routeService

    Point tourist   = new Point(lat: 48.2678547, lon: 25.9267581)
    Point fastivska = new Point(lat: 48.2824300, lon: 25.9748600)
    Point epicentr  = new Point(lat: 48.3190400, lon: 25.9391500)
    Point mist      = new Point(lat: 48.28950,   lon: 25.95100  )
    Point pdKilts   = new Point(lat: 48.25217,   lon: 25.93817  )
    Point end1a   = new Point(lat: 48.25115,     lon: 25.95451  )
    Point chornivka = new Point(lat: 48.37229,   lon: 26.00000  )
    Point prospHolovna=new Point(lat: 48.27028,  lon: 25.94808)
    Point graviton  = new Point(lat: 48.27329,   lon: 25.99360)
    Point kalinka   = new Point(lat: 48.31065,   lon: 25.95938)

    @Test
    void "test simple route without switches (use 34 only)"() {
        def route = routeService.findSimpleRoute(fastivska, epicentr)
        assert [[[], ["34"], []]] == route.routeChunks.route.ref
    }

    @Test
    void "test simple route with parallelism (use bus 34 or troll 2)"() {
        def route = routeService.findSimpleRoute(fastivska, mist)
        assert [[[], ["34", "2"], []]] == route.routeChunks.route.ref
    }

    @Test
    void "test route with one switch (to center, from center), parallel"() {
        def route = routeService.findRouteWithOneSwitchWithGaps(end1a, chornivka)
        assert [
                [[], ["3а", "1а"], ["36", "37"], []],
                [[], ["12"], [], ["36", "37"], []],
                [[], ["2"], ["37", "28"], []]
        ] == route.routeChunks.route.ref
    }

    @Test
    @Ignore
    void "test route with one switch (to center, from center)"() {
        def route = routeService.findRouteWithOneSwitchWithGaps(prospHolovna, chornivka)
        assert [
                [[], ["3а", "1а"], ["36", "37"], []],
                [[], ["12"], [], ["36", "37"], []],
                [[], ["2"], ["37", "28"], []]
        ] == route.routeChunks.route.ref
    }

    @Test
    void "test simple with one switch"() {
        def route = routeService.findRouteWithOneSwitchWithGaps(graviton, kalinka)

        println route.routeChunks.route.ref
    }

    @BeforeClass
    static void loadData() {
        TransportStorage transportStorage = new TransportStorage(
                matrixProcess: new MatrixProcess(),
                maxDistance: 500,
                filename: "transport_test.osm",
                transportDataProvider: new TransportDataProvider()
        )
        transportStorage.init()
        stopService = new StopService(maxDistance: 500, transportStorage: transportStorage)
        routeService = new RouteService(transportStorage: transportStorage, stopService: stopService)
    }
}
