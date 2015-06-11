package dospring.service

import dospring.controllers.model.CalculatedRoute
import dospring.controllers.model.RouteChunk
import model.Route
import org.springframework.stereotype.Service

@Service
class RouteSimplifierService {

    List<CalculatedRoute> dropSimilarRoutes(List<CalculatedRoute> routes) {
        List<CalculatedRoute> routesToRemove = []
        List<CalculatedRoute> processed = []
        for (int i = 0; i < routes.size() - 1; i++) {
            def r1 = routes[i]
            if (processed.contains(r1)) {
                continue
            }
            List<CalculatedRoute> similar = [r1]

            while(true) {
                def similarStep = []
                routes.findAll {
                    !similar.contains(it) && !processed.contains(it) &&
                            r1.routeChunks.count {it.route } == it.routeChunks.count { it.route }
                }
                .each { test ->
                    if (similar.any { orig ->
                        List<RouteChunk> copyChunks1 = orig.routeChunks.findAll { it.route }
                        List<RouteChunk> copyChunks2 = test.routeChunks.findAll { it.route }
                        (0..<copyChunks1.size()).every { Integer idx ->
                            routesSimilar(copyChunks1[idx].route, copyChunks2[idx].route)
                        }
                    }) {
                        similarStep << test
                    }
                }
                similar.addAll(similarStep)
                if (!similarStep) break
            }

            if (similar.size() != 1) {
                processed += similar
                // max by multiplication of the |routes|
                similar.sort {r -> r.routeChunks.sum {it.end - r.routeChunks.last().end}} // least important - route chunk ends as close to the destination as possible
                similar.sort {it.routeChunks.findAll {!it.route}.sum {it.start - it.end}} // then by overall walk distance
                similar.sort {it.routeChunks.collect {it.route.size()}.inject (-1, {a,b->a*b})} // most important - more routes for the path
                def winner = similar.first()
                routesToRemove += similar - winner
            }
        }
        routes - routesToRemove
    }

    private boolean routesSimilar(List<Route> r1, List<Route> r2) {
        if (!r1 && !r2) {
            return true
        }
        def intersects = r1.intersect(r2).size()
        if (intersects >= 2)
            return true
        if (intersects == 1 && (r1.size() <= 2 || r2.size() <= 2)) {
            return true
        }
        false
    }
}
