package dospring.controllers

import dospring.controllers.model.CalculatedRoute
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

    @RequestMapping('/stops')
    List<Stop> someStops() {
        return transportStorage.stops.take(10)
    }

    @RequestMapping('/route/from/{from}/to/{to}/switches/{switches}')
    List<CalculatedRoute> findRoute(@PathVariable("from") Point from,
                                    @PathVariable("to") Point to,
                                    @PathVariable("switches") Integer switches) {
        switch (switches) {
            case null:
            case 0:
                return routeService.findSimpleRoute(from, to)
            /*case 1:
                return routeService.findRouteWithOneSwitchWithGaps(from, to)
            case 2:
                return routeService.findRouteWithTwoSwitchesAndGaps(from, to)*/
            default:
                return null
        }
    }

    @RequestMapping("/stop/{stop}/nearest")
    String findStop(@PathVariable("stop") Point point) {
        stopService.findNearestStop(point)?:"Not Found"
    }

    @RequestMapping("/stop/{stop}/all")
    String findNearestStops(@PathVariable("stop") Point point) {
        stopService.findNearestStops(point)
    }
}
