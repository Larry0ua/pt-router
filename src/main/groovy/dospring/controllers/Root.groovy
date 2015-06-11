package dospring.controllers

import dospring.service.RouteService
import dospring.service.StopService
import dospring.storage.parser.TransportStorage
import model.Point
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
        } else {
            return null
        }
    }

    @RequestMapping('/city/{city}/route/from/{from}/to/{to}/all')
    String findRoute(@PathVariable("city") String city,
                         @PathVariable("from") Point from,
                         @PathVariable("to") Point to) {
        if (city == 'chernivtsi') {

            def routes = routeService.findSimpleRoute(from, to)

            routes.routeChunks.routes.name
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
