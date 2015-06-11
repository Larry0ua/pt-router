package dospring.service
import dospring.controllers.model.CalculatedRoute
import dospring.controllers.model.RouteChunk
import org.springframework.stereotype.Service

@Service
class RouteSimplifierService {

    List<CalculatedRoute> dropSimilarRoutes(List<CalculatedRoute> routes) {

        def similars = groupBySimilarity(routes, { CalculatedRoute orig, CalculatedRoute test ->
            List<RouteChunk> copy1 = orig.routeChunks.findAll { it.route }
            List<RouteChunk> copy2 = test.routeChunks.findAll { it.route }
            copy1.size() == copy2.size() && (0..<copy1.size()).every{Integer idx -> copy1[idx].route == copy2[idx].route}
        })
        def routes2 = similars.collect { group -> group.sort { it.routeChunks.sum{ it.start - it.end}}.first()}

        def routes3 = groupBySimilarity(routes2, { CalculatedRoute orig, CalculatedRoute test ->
            List<RouteChunk> copy1 = orig.routeChunks.findAll { it.route }
            List<RouteChunk> copy2 = test.routeChunks.findAll { it.route }
            copy1.size() == copy2.size() && (0..<copy1.size()).every{Integer idx ->
                copy1[idx].route.containsAll(copy2[idx].route) || copy2[idx].route.containsAll(copy1[idx].route)
            }
        }).collect { group -> group.max { it.routeChunks.collect {it.route.size()}.findAll{it}.inject(1, {a,b->a*b}) } }

        routes3
    }

    private List<List<CalculatedRoute>> groupBySimilarity(List<CalculatedRoute> routes, Closure<Boolean> areSimilar) {
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
