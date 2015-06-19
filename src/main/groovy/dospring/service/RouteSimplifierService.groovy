package dospring.service
import dospring.controllers.model.CalculatedRoute
import dospring.controllers.model.RouteChunk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class RouteSimplifierService {

    @Autowired
    RouteOrderingService orderingService

    @Value('${order.merge.wait.threshold}')
    Integer mergeThreshold

    List<CalculatedRoute> dropSimilarRoutes(List<CalculatedRoute> routes) {

        def routeTimes = orderingService.calculateRouteTimes(routes)

        // 1. drop equal routes with different walk times, minimize by overall time
        def routes2 = groupBySimilarity(routes, { CalculatedRoute orig, CalculatedRoute test ->
            List<RouteChunk> copy1 = orig.routeChunks.findAll { it.routes }
            List<RouteChunk> copy2 = test.routeChunks.findAll { it.routes }
            copy1.size() == copy2.size() && (0..<copy1.size()).every{Integer idx -> copy1[idx].routes == copy2[idx].routes}
        }).collect { group -> group.min {routeTimes[it]} }

        // route [1] and route [1,2] should be grouped and then merged only if time difference is <mergeThreshold and [1] is faster.
        // if [1,2] is faster, [1] should be merged anyway
        def routes3 = groupBySimilarity(routes2, { CalculatedRoute r1, CalculatedRoute r2 ->
            List<RouteChunk> copy1 = r1.routeChunks.findAll { it.routes }
            List<RouteChunk> copy2 = r2.routeChunks.findAll { it.routes }
            if (copy1.size() == copy2.size()) {
                def larger1st = (0..<copy1.size()).every { Integer idx -> copy1[idx].routes.containsAll(copy2[idx].routes) }
                def larger2nd = (0..<copy1.size()).every { Integer idx -> copy2[idx].routes.containsAll(copy1[idx].routes) }
                if (larger1st || larger2nd) {
                    def r1time = routeTimes[r1]
                    def r2time = routeTimes[r2]
                    larger1st && (r1time < r2time || r1time - r2time < mergeThreshold) ||
                            larger2nd && (r2time < r1time || r2time - r1time < mergeThreshold)
                } else {
                    false
                }
            } else {
                false
            }
        }).collect { group -> group.max { it.routeChunks.collect {it.routes.size()}.findAll{it}.inject(1, {a,b->a*b}) } }

        routes3
    }

    private static <T> List<List<T>> groupBySimilarity(Collection<T> routes, Closure<Boolean> areSimilar) {
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
