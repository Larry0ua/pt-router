package dospring.processor.matrix;

import model.Route;
import model.Stop;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

@Component
public class MatrixProcess {

    public Map<Stop, Map<Stop, List<Route>>> prepareMatrix(Collection<Route> routes) {
        assert routes != null;

        Map<Stop, Set<Route>> stopsToRoutes =
                routes.stream()
                .flatMap(route ->
                        route.getPlatforms().stream()
                                .map(platform -> Pair.of(platform, route))
                )
                .collect(groupingBy(Pair::k, mapping(Pair::v, toSet())));


        // matrix is about ways to travel from stop1 to stop2: stop1 -> set<route> -> stop2
        // stopsToRoutes.stop -> stopsToRoutes.route (stop -> addToSet route)
        Map<Stop, Map<Stop, List<Route>>> matrix =
                stopsToRoutes.entrySet().stream()
                // stop -> set<route>
                .flatMap(entry -> entry.getValue().stream()
                        .map(route -> new Pair<>(entry.getKey(), route)))
                        // pair<stop, route>
                .flatMap(pair ->
                                IntStream.range(pair.v().getPlatforms().indexOf(pair.k()) + 1,
                                        pair.v().getPlatforms().size())
                                        .mapToObj(i -> pair.v().getPlatforms().get(i))
                                        .map(stop2 -> Pair.of(pair.k(), Pair.of(stop2, pair.v())))
                )
                .collect(
                        groupingBy(Pair::k, // first, group by stop 'from'
                                groupingBy(pair -> pair.v().k(), // then by stop 'to'
                                        mapping(pair -> pair.v().v(), toList()) // and gather routes, eliminating possible duplicates
                                )
                        )
                );

        return matrix;
    }

}
