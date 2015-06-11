package rest.service

import dospring.controllers.model.CalculatedRoute
import dospring.controllers.model.RouteChunk
import dospring.service.RouteSimplifierService
import model.Point
import model.Route
import org.junit.Before
import org.junit.Test

class SimplificationServiceTest {

    Point p1 = new Point(lat:0,lon:0)
    Point p2 = new Point(lat:0,lon:1)
    Point p3 = new Point(lat:0,lon:2)
    Point p4 = new Point(lat:0,lon:3)
    Route r1 = new Route(ref:"1")
    Route r2 = new Route(ref:"2")
    Route r3 = new Route(ref:"3")
    Route r4 = new Route(ref:"4")
    Route r5 = new Route(ref:"5")

    RouteSimplifierService service

    @Test
    void "test merge subsets"() {
        def route = [
                CalculatedRoute.createRoute([
                        new RouteChunk([r1], p1, p2),
                        new RouteChunk([r2,r3], p2, p3)
                ]),
                CalculatedRoute.createRoute([
                        new RouteChunk([r1], p1, p2),
                        new RouteChunk([r2], p2, p3),
                ])
        ]

        assert [[["1"],["2","3"]]] == service.dropSimilarRoutes(route).routeChunks.routes.ref
    }

    @Before
    void init() {
        service = new RouteSimplifierService()
    }
}
