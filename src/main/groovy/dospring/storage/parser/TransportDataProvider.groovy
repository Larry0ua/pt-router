package dospring.storage.parser
import model.Route
import model.Stop
import org.springframework.stereotype.Service

@Service
class TransportDataProvider {

    def parseFile(String filename) {
        Map<String, Stop> stops = [:]
        Map<String, Route> routes = [:]

        List<String> supportedTypes = ['bus', 'trolleybus', 'share_taxi', 'tram']

        new OsmTransportParser(
                filename: this.class.classLoader.getResource(filename).toString(),
                processNode: { attributes, tags ->
                    if (tags.public_transport == 'platform' || tags.highway == 'bus_stop') {
                        stops[attributes.id] = new Stop(
                                lat: attributes.lat.toDouble(),
                                lon: attributes.lon.toDouble(),
                                id: attributes.id,
                                name: tags.name
                        )
                    }
                },
                processRelation: { attributes, tags, members ->
                    if (tags.type == 'route' && tags.route in supportedTypes) {
                        Collection<Stop> routeStops = []
                        members.findAll {it.type == 'node' && it.role.startsWith('platform')}*.ref.each { String id ->
                            if (stops[id]) {
                                routeStops << stops[id]
                            } else {
                                println "Stop not found $id"
                            }
                        }
                        routes[attributes.id] = new Route(
                                platforms: routeStops,
                                ref: tags.ref,
                                type: tags.route,
                                name: tags.name,
                                id: attributes.id
                        )
                    }
                }
        ).process()
        [routes, stops]
    }
}
