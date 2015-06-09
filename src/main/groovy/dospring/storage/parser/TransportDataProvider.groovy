package dospring.storage.parser

import model.Route
import model.Stop
import org.springframework.stereotype.Component

@Component
class TransportDataProvider {

    Collection<Stop> stops
    Collection<Route> routes

    def parseFile(String filename) {
        Map<String, Stop> stopsMap = [:]
        List<Route> routes = []

        List<String> supportedTypes = ['bus', 'trolleybus', 'share_taxi', 'tram']

        new OsmTransportParser(
                filename: this.class.classLoader.getResource(filename).toString(),
                processNode: { attributes, tags ->
                    if (tags.public_transport == 'platform' || tags.highway == 'bus_stop') {
                        stopsMap[attributes.id] = new Stop(
                                lat: attributes.lat.toDouble(),
                                lon: attributes.lon.toDouble(),
                                id: attributes.id,
                                name: tags.name
                        )
                    }
                },
                processRelation: { attributes, tags, members ->
                    if (tags.type == 'route' && tags.route in supportedTypes) {
                        Collection<Stop> stops = []
                        members.findAll {it.type == 'node' && it.role.startsWith('platform')}*.ref.each {
                            if (stopsMap[it]) {
                                stops << stopsMap[it]
                            } else {
                                println "Stop not found $it"
                            }
                        }
                        routes << new Route(
                                platforms: stops,
                                ref: tags.ref,
                                type: tags.route,
                                name: tags.name,
                                id: attributes.id
                        )
                    }
                }
        ).process()
        this.stops = stopsMap.values()
        this.routes = routes
    }
}
