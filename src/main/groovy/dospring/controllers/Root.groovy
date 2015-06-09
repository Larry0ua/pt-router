package dospring.controllers

import dospring.service.RouteService
import dospring.service.StopService
import dospring.storage.parser.TransportStorage
import model.Point
import model.Route
import model.Stop
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Root {

    @Autowired
    TransportStorage transportStorage

    @Autowired
    StopService stopService

    @Autowired
    RouteService routeService

    @RequestMapping('/city/{city}/stops')
    List<Stop> greeting(@PathVariable("city") String city) {
        if (city == 'chernivtsi') {

            return transportStorage.stops.take(10)
//            Point point1 = new Point(lat:48.26892, lon:25.92709)
//            Point point2 = new Point(lat:48.28331, lon:25.97252)
//            Point point3 = new Point(lat:48.28972, lon:25.95090)
//
//            def byDist1 = transportStorage.stops.sort { it - point1 }
//            def byDist2 = transportStorage.stops.sort { it - point3 }
//
//            Collection<Stop> stop1 = (byDist1.findAll{it - point1 < 500} + byDist1.take(4)).toSet()
//            Collection<Stop> stop2 = (byDist2.findAll{it - point3 < 500} + byDist2.take(4)).toSet()
//
//            Set<Route> available = []
//            stop1.each {
//                it1 -> stop2.each {
//                    it2 ->
//                        println "From ${it1.name} (${it1.id}) to ${it2.name} (${it2.id})"
//                        transportStorage.matrix.get(it1)?.get(it2)?.each {
//                            available << it
//                            println "    By ${it.name} ${it.id}"
//                        }
//                }
//            }
//            return available.collect { "$it.name $it.id\n"}
        } else {
            return null
        }
    }

    @RequestMapping('/city/{city}/route/from/{from}/to/{to}/all')
    String findRoute(@PathVariable("city") String city,
                         @PathVariable("from") Point from,
                         @PathVariable("to") Point to) {
        if (city == 'chernivtsi') {

            Collection<Stop> stop1 = stopService.findNearestStops(from).take(4).toSet()
            Collection<Stop> stop2 = stopService.findNearestStops(to).take(4).toSet()

            Set<Route> available = []
            routeService.findSimpleRoute(stop1, stop2).collect { "$it.name $it.id\n" }
        } else {
            "No route for city $city yet!"
        }
    }

    @RequestMapping("/city/{city}/stop/{stop}/nearest")
    String findStop(@PathVariable("city") String city,
                        @PathVariable("stop") Point point) {
        stopService.findNearestStop(point)?:"Not Found"
    }

    @RequestMapping("/city/{city}/stop/{stop}/all")
    String findNearestStops(@PathVariable("city") String city,
                            @PathVariable("stop") Point point) {
        stopService.findNearestStops(point)
    }
}
