package dospring.storage.parser

import model.Point
import model.Route
import model.Stop
import org.springframework.stereotype.Service
import org.xml.sax.Attributes

@Service
class TransportDataProvider {

    public static final long WAY_ADDER = 100_000_000_000
    public static final long REL_ADDER = 200_000_000_000

    def parseFile(String filename) {
        Map<Long, Stop> stops = [:]
        Map<Long, Route> routes = [:]
        Map<Long, Point> nodes = [:]

        List<String> supportedTypes = ['bus', 'trolleybus', 'share_taxi', 'tram']

        new OsmTransportParser(
                filename: this.class.classLoader.getResource(filename).toString(),
                processNode: { Map<String, String> attributes, Map<String, String> tags ->
                    if (tags.public_transport == 'platform' || tags.highway == 'bus_stop') {
                        stops[attributes.id.toLong()] = new Stop(
                                lat: attributes.lat.toDouble(),
                                lon: attributes.lon.toDouble(),
                                id: attributes.id,
                                name: tags.name
                        )
                    }
                    nodes[attributes.id.toLong()] = new Point(
                            lat: attributes.lat.toDouble(),
                            lon: attributes.lon.toDouble()
                    )
                },
                processWay: { Map<String, String> attributes, Map<String, String> tags, List<Long> nd ->
                    if (tags.public_transport == 'platform' || tags.highway == 'bus_stop') {
                        def point = nodes[nd[0]]
                        if (!point) {
                            println "Point ${nd[0]} was not found"
                            return
                        }
                        stops[attributes.id.toLong() + WAY_ADDER] = new Stop(
                                lat: point.lat,
                                lon: point.lon,
                                id: attributes.id.toLong() + WAY_ADDER,
                                name: tags.name
                        )
                    }
                },
                processRelation: { Map<String, String> attributes, Map<String, String> tags, Collection<Member> members ->
                    if (tags.type == 'route' && tags.route in supportedTypes) {
                        Collection<Stop> routeStops = []
                        members.findAll {it.role.startsWith('platform')}.each { Member member ->
                            def id = member.ref
                            if (member.type == 'way') {
                                id += WAY_ADDER
                            }
                            if (stops[id]) {
                                routeStops << stops[id]
                            } else {
                                println "Stop not found $id"
                            }
                        }
                        routes[attributes.id.toLong()] = new Route(
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
