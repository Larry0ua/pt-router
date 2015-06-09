package rest.model

import dospring.controllers.model.CalculatedRoute
import dospring.controllers.model.RouteChunk
import model.Route
import model.Stop
import org.junit.Test

class CalculatedRouteTest {

    @Test
    void "test calculated route is created and appended"() {
        def route1 = new Route(ref: 5)
        def route2 = new Route(ref: 7)
        def stop1 = new Stop(name: "stop1")
        def stop2 = new Stop(name: "stop2")
        def stop3 = new Stop(name: "stop3")
        def stop4 = new Stop(name: "stop4")
        def chunk1 = new RouteChunk([route1], stop1, stop2)
        def chunk2 = new RouteChunk([route2], stop2, stop3)

        def cr = CalculatedRoute.createRoute(chunk1)
        def cr2 = cr.append(chunk2)

        assert [chunk1, chunk2] == cr2.routeChunks
    }
}
