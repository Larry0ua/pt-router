package dospring.service
import dospring.storage.parser.TransportStorage
import model.Point
import model.Stop
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class StopService {

    @Value('${maxDist}')
    double maxDistance

    @Autowired
    TransportStorage transportStorage

    Stop findNearestStop(Point start) {
        if (!start) {
            return null
        }

        def nearestStop = transportStorage.stops.min { it - start }
        if (start - nearestStop > maxDistance) {
            null
        } else {
            nearestStop
        }
    }

    @Cacheable(value = "nearestStops", key="#start.lat+','+#start.lon")
    List<Stop> findNearestStops(Point start) {
        if (!start) {
            return []
        }

        transportStorage.stops.findAll{it - start < maxDistance}.sort{it - start}
    }
}
