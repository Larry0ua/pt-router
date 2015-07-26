package dospring.storage.parser

import model.Route
import model.Stop
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
@Scope
class TransportStorage {

    Collection<Route> routes
    Collection<Stop> stops
    Map<Stop, Map<Stop, Double>> walkMatrix
    Map<Stop, Map<Stop, Set<Route>>> matrix
    Map<Stop, Set<Route>> stopsToRoutes
    StopsStorage stopsStorage

    @Value('${maxDist}')
    double maxWalkDistance

    @Value('${cities}')
    String definedCities

    @Autowired
    TransportDataProvider transportDataProvider

    @PostConstruct
    def init() {
        def allRoutesMap = [:]
        def allStopsMap  = [:]
        definedCities.split(',').each {
            def out = transportDataProvider.parseFile(it.trim())
            allRoutesMap.putAll(out[0])
            allStopsMap.putAll(out[1])
        }

        routes = allRoutesMap.values().sort()
        stops = allStopsMap.values().sort()

        stopsStorage = new StopsStorage()
        stopsStorage.putAll(stops)

        stopsToRoutes = [:].withDefault { [].toSet() }
        routes.each { Route it ->
            it.platforms.each { Stop stop ->
                stopsToRoutes[stop] << it
            }
        }

        matrix = [:].withDefault { [:].withDefault { [].toSet() } }

        stops.each { Stop from ->
            def rts = stopsToRoutes[from]
            rts.collect {r -> r.after(from)}.flatten().toSet().each { Stop to ->
                matrix[from][to] = stopsToRoutes[from].intersect(stopsToRoutes[to]).findAll{it.isAfter(from, to)}
            }
        }
    }

    Map<Stop, Map<Stop, Double>> prepareWalkMatrix() {
        Map<Stop, Map<Stop, Double>> result = [:]
        stops.each { f ->
            stops.each { t ->
                if (f != t && (f - t) < maxWalkDistance) {
                    if (result[f] == null) {
                        result[f] = [:]
                    }
                    result[f][t] = f-t
                }
            }
        }
        walkMatrix = result
    }
}
