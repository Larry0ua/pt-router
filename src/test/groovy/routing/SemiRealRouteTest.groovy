package routing

import org.junit.Test

class SemiRealRouteTest extends SemiRealRouteData {

    @Test
    void "test switch is at the farthest possible point"() {
        def route = routeService.findRouteWithOneSwitchWithGaps(s.s1, s.s9)
        assert [[["1a", "3a"], ["36", "37"]], [[], ["12"], ["36", "37"]]] == route.routeChunks.routes.ref
    }

}
