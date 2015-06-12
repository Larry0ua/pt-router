package rest.service
import dospring.service.RouteOrderingService
import org.junit.Before
import org.junit.Test
import routing.SemiRealRouteData

class SimpleDataRouteOrderingServiceTest extends SemiRealRouteData {

    RouteOrderingService routeOrderingService

    @Test
    void "test simple route ordering"() {
        def routes = routeService.findRouteWithOneSwitchWithGaps(s.s1, s.s9)
        routes = routeOrderingService.sortRoutes(routes)
        assert [[["1a", "3a"], ["36", "37"]], [[], ["12"], ["36", "37"]]] == routes.routeChunks.routes.ref
    }

    @Before
    void initOrderingService() {
        routeOrderingService = new RouteOrderingService()
    }
}
