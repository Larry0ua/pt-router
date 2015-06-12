package dospring.service

import dospring.controllers.model.CalculatedRoute
import dospring.controllers.model.RouteChunk
import model.Route
import org.springframework.stereotype.Service

@Service
class RouteOrderingService {

    List<CalculatedRoute> sortRoutes(List<CalculatedRoute> routes) {
        // time for route: 10 * number of switches from route to route + 2 * each stop travelled (average) + 6 * distance walked (km)
        Map<CalculatedRoute, BigDecimal> sortedRoutes = routes.collectEntries {[it,
                                10 * it.routeChunks.count{it.routes} + // each route switch would take 10 minutes
                                        6 * (it.routeChunks.findAll{!it.routes}.sum {it.start - it.end}?:0) + // 6 minutes for each walked km
                                        2 * (it.routeChunks.findAll{it.routes}.sum{ RouteChunk rc ->
                                    rc.routes.sum{Route r-> r.countBetween(rc.start, rc.end)} / rc.routes.size()
                                }?:0) // 2 * average count of stops travelled
        ]}
        sortedRoutes.sort {it.value}.keySet().toList()
    }
}
