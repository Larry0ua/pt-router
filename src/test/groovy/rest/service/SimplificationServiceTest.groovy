package rest.service

import dospring.controllers.model.CalculatedRoute
import dospring.controllers.model.RouteChunk
import model.Point
import org.junit.Test

class SimplificationServiceTest {

    Point p1 = new Point(lat: 0, lon: 0)

    @Test
    void "test easy simplification"() {
        def route = [
                CalculatedRoute.createRoute([
                        new RouteChunk([], p1, p1)
                ]),
                CalculatedRoute.createRoute([

                ])
        ]
    }
}
