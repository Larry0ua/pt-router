package dospring.service
import dospring.controllers.model.CalculatedRoute
import dospring.controllers.model.RouteChunk
import org.springframework.stereotype.Service

@Service
class RouteSimplifierService {

    List<CalculatedRoute> dropSimilarRoutes(List<CalculatedRoute> routes) {

        def routes2 = groupBySimilarity(routes, { CalculatedRoute orig, CalculatedRoute test ->
            List<RouteChunk> copy1 = orig.routeChunks.findAll { it.routes }
            List<RouteChunk> copy2 = test.routeChunks.findAll { it.routes }
            copy1.size() == copy2.size() && (0..<copy1.size()).every{Integer idx -> copy1[idx].routes == copy2[idx].routes}
        }).collect {
            group -> group.sort {
                it.routeChunks.sum{ it.start - it.end} + (it.routeChunks.findAll{!it.routes}.sum{it.start - it.end}?:0) * 10
            }.first()
        }

        def routes3 = groupBySimilarity(routes2, { CalculatedRoute orig, CalculatedRoute test ->
            List<RouteChunk> copy1 = orig.routeChunks.findAll { it.routes }
            List<RouteChunk> copy2 = test.routeChunks.findAll { it.routes }
            copy1.size() == copy2.size() && (0..<copy1.size()).every{Integer idx ->
                copy1[idx].routes.containsAll(copy2[idx].routes) || copy2[idx].routes.containsAll(copy1[idx].routes)
            }
        }).collect { group -> group.max { it.routeChunks.collect {it.routes.size()}.findAll{it}.inject(1, {a,b->a*b}) } }

        routes3
    }

    private static List<List<CalculatedRoute>> groupBySimilarity(List<CalculatedRoute> routes, Closure<Boolean> areSimilar) {
        def result = []
        def processed = []
        routes.each { r1 ->
            if (!processed.contains(r1)) {
                processed += r1
                def thisSimilar = [r1]
                while (true) {
                    def step = routes.findAll { test -> !processed.contains(test) && thisSimilar.any {areSimilar.call(it, test)}}

                    thisSimilar += step
                    processed += step
                    if (!step) break
                }
                result << thisSimilar
            }
        }
        result
    }
}
