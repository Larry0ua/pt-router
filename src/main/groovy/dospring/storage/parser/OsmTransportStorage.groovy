package dospring.storage.parser
import dospring.processor.matrix.MatrixProcess
import model.Route
import model.Stop
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
@Scope() // singleton
class OsmTransportStorage {

    Collection<Route> routes
    Collection<Stop> stops
    Map<Stop, Map<Stop, Set<Route>>> matrix
    Map<Stop, Map<Stop, Double>> walkMatrix

    @Value('${filename}')
    String filename

    @Value('${maxDist}')
    double maxDistance

    @Autowired
    MatrixProcess matrixProcess

    @Autowired
    TransportDataProvider transportDataProvider

    @PostConstruct
    def init() {
        transportDataProvider.parseFile(filename)
        this.routes = transportDataProvider.routes
        this.stops = transportDataProvider.stops

        walkMatrix = prepareWalkMatrix(stops)
        matrix = matrixProcess.prepareMatrix(routes)
    }

    Map<Stop, Map<Stop, Double>> prepareWalkMatrix(Collection<Stop> stops) {
        Map<Stop, Map<Stop, Double>> result = [:]
        stops.each { f ->
            stops.each { t ->
                if (f != t && (f - t) < maxDistance) {
                    if (result[f] == null) {
                        result[f] = [:]
                    }
                    result[f][t] = f-t
                }
            }
        }
        result
    }
}
