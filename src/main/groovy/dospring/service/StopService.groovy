package dospring.service
import dospring.storage.parser.OsmTransportStorage
import model.Point
import model.Stop
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class StopService {

    @Value('${maxDist}')
    double maxDistance

    @Autowired
    OsmTransportStorage transportStorage

    Stop findNearestStop(Point start) {
        def nearestStop = transportStorage.stops.min { it - start }
        if (start - nearestStop > maxDistance) {
            null
        } else {
            nearestStop
        }
    }

    List<Stop> findNearestStops(Point start) {
        transportStorage.stops.findAll{it - start < maxDistance}.sort{it - start}
    }
}
